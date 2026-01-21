package com.fluxmall.address.service;

import com.fluxmall.address.domain.ShippingAddress;
import com.fluxmall.address.dto.request.AddressCreateRequest;
import com.fluxmall.address.dto.request.AddressUpdateRequest;
import com.fluxmall.address.dto.response.AddressResponse;
import com.fluxmall.address.exception.AddressError;
import com.fluxmall.address.repository.ShippingAddressMapper;
import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.support.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingAddressService 테스트")
class ShippingAddressServiceTest {

    @Mock
    private ShippingAddressMapper shippingAddressMapper;

    @InjectMocks
    private ShippingAddressService shippingAddressService;

    @Nested
    @DisplayName("getAddresses: 배송지 목록 조회")
    class GetAddresses {

        @Test
        @DisplayName("성공: 배송지 목록 조회")
        void 배송지_목록_조회_성공() {
            // given
            Long memberId = 1L;
            List<ShippingAddress> addresses = TestFixture.createShippingAddresses(memberId);

            given(shippingAddressMapper.findByMemberId(memberId)).willReturn(addresses);

            // when
            List<AddressResponse> response = shippingAddressService.getAddresses(memberId);

            // then
            assertThat(response).hasSize(3);
        }

        @Test
        @DisplayName("성공: 배송지 없으면 빈 목록 반환")
        void 배송지_없으면_빈_목록() {
            // given
            Long memberId = 1L;

            given(shippingAddressMapper.findByMemberId(memberId)).willReturn(List.of());

            // when
            List<AddressResponse> response = shippingAddressService.getAddresses(memberId);

            // then
            assertThat(response).isEmpty();
        }
    }

    @Nested
    @DisplayName("createAddress: 배송지 생성")
    class CreateAddress {

        @Test
        @DisplayName("성공: 첫 배송지 생성 (자동 기본 배송지)")
        void 첫_배송지_생성_자동_기본() {
            // given
            Long memberId = 1L;
            AddressCreateRequest request = new AddressCreateRequest(
                    "홍길동",
                    "010-1234-5678",
                    "12345",
                    "서울시 강남구 테스트로 123",
                    "101동 101호",
                    false
            );
            ShippingAddress savedAddress = TestFixture.createShippingAddress(1L, memberId, true);

            given(shippingAddressMapper.findByMemberId(memberId)).willReturn(List.of());

            willAnswer(invocation -> {
                ShippingAddress address = invocation.getArgument(0);
                setFieldValue(address, "id", 1L);
                return null;
            }).given(shippingAddressMapper).insert(any(ShippingAddress.class));

            given(shippingAddressMapper.findById(1L)).willReturn(savedAddress);

            // when
            AddressResponse response = shippingAddressService.createAddress(memberId, request);

            // then
            assertThat(response).isNotNull();
            verify(shippingAddressMapper).clearDefaultByMemberId(memberId);
            verify(shippingAddressMapper).insert(any(ShippingAddress.class));
        }

        @Test
        @DisplayName("성공: 기본 배송지로 설정 요청")
        void 기본_배송지로_설정_요청() {
            // given
            Long memberId = 1L;
            AddressCreateRequest request = new AddressCreateRequest(
                    "홍길동",
                    "010-1234-5678",
                    "12345",
                    "서울시 강남구 테스트로 123",
                    "101동 101호",
                    true  // 기본 배송지로 설정 요청
            );
            List<ShippingAddress> existingAddresses = TestFixture.createShippingAddresses(memberId);
            ShippingAddress savedAddress = TestFixture.createShippingAddress(4L, memberId, true);

            given(shippingAddressMapper.findByMemberId(memberId)).willReturn(existingAddresses);

            willAnswer(invocation -> {
                ShippingAddress address = invocation.getArgument(0);
                setFieldValue(address, "id", 4L);
                return null;
            }).given(shippingAddressMapper).insert(any(ShippingAddress.class));

            given(shippingAddressMapper.findById(4L)).willReturn(savedAddress);

            // when
            AddressResponse response = shippingAddressService.createAddress(memberId, request);

            // then
            assertThat(response).isNotNull();
            verify(shippingAddressMapper).clearDefaultByMemberId(memberId);
        }

        @Test
        @DisplayName("실패: 최대 개수 초과")
        void 최대_개수_초과() {
            // given
            Long memberId = 1L;
            AddressCreateRequest request = new AddressCreateRequest(
                    "홍길동",
                    "010-1234-5678",
                    "12345",
                    "서울시 강남구",
                    "101호",
                    false
            );

            // 10개의 배송지가 이미 존재
            List<ShippingAddress> existingAddresses = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                existingAddresses.add(TestFixture.createShippingAddress((long) i, memberId, i == 1));
            }

            given(shippingAddressMapper.findByMemberId(memberId)).willReturn(existingAddresses);

            // when & then
            assertThatThrownBy(() -> shippingAddressService.createAddress(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AddressError.MAX_ADDRESS_EXCEEDED);
        }

        private void setFieldValue(Object object, String fieldName, Object value) {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("updateAddress: 배송지 수정")
    class UpdateAddress {

        @Test
        @DisplayName("성공: 배송지 수정")
        void 배송지_수정_성공() {
            // given
            Long memberId = 1L;
            Long addressId = 1L;
            ShippingAddress address = TestFixture.createShippingAddress(addressId, memberId, true);
            AddressUpdateRequest request = new AddressUpdateRequest(
                    "김철수",
                    "010-9999-8888",
                    null,
                    null,
                    null
            );
            ShippingAddress updatedAddress = TestFixture.createShippingAddress(addressId, memberId, true);

            given(shippingAddressMapper.findById(addressId)).willReturn(address, updatedAddress);

            // when
            AddressResponse response = shippingAddressService.updateAddress(memberId, addressId, request);

            // then
            assertThat(response).isNotNull();
            verify(shippingAddressMapper).update(any(ShippingAddress.class));
        }

        @Test
        @DisplayName("실패: 배송지 없음")
        void 배송지_없음() {
            // given
            Long memberId = 1L;
            Long addressId = 999L;
            AddressUpdateRequest request = new AddressUpdateRequest("수정", null, null, null, null);

            given(shippingAddressMapper.findById(addressId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> shippingAddressService.updateAddress(memberId, addressId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AddressError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void 권한_없음() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long addressId = 1L;
            ShippingAddress address = TestFixture.createShippingAddress(addressId, otherMemberId, true);
            AddressUpdateRequest request = new AddressUpdateRequest("수정", null, null, null, null);

            given(shippingAddressMapper.findById(addressId)).willReturn(address);

            // when & then
            assertThatThrownBy(() -> shippingAddressService.updateAddress(memberId, addressId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AddressError.NOT_OWNER);
        }
    }

    @Nested
    @DisplayName("deleteAddress: 배송지 삭제")
    class DeleteAddress {

        @Test
        @DisplayName("성공: 일반 배송지 삭제")
        void 일반_배송지_삭제_성공() {
            // given
            Long memberId = 1L;
            Long addressId = 2L;  // 기본 배송지가 아닌 것
            ShippingAddress address = TestFixture.createShippingAddress(addressId, memberId, false);

            given(shippingAddressMapper.findById(addressId)).willReturn(address);

            // when
            shippingAddressService.deleteAddress(memberId, addressId);

            // then
            verify(shippingAddressMapper).delete(addressId);
            verify(shippingAddressMapper, never()).setDefault(anyLong());
        }

        @Test
        @DisplayName("성공: 기본 배송지 삭제 시 다른 배송지가 기본으로 설정")
        void 기본_배송지_삭제_다른_배송지_기본_설정() {
            // given
            Long memberId = 1L;
            Long addressId = 1L;  // 기본 배송지
            ShippingAddress defaultAddress = TestFixture.createShippingAddress(addressId, memberId, true);
            List<ShippingAddress> allAddresses = new ArrayList<>(TestFixture.createShippingAddresses(memberId));

            given(shippingAddressMapper.findById(addressId)).willReturn(defaultAddress);
            given(shippingAddressMapper.findByMemberId(memberId)).willReturn(allAddresses);

            // when
            shippingAddressService.deleteAddress(memberId, addressId);

            // then
            verify(shippingAddressMapper).delete(addressId);
            verify(shippingAddressMapper).setDefault(anyLong());  // 다른 배송지가 기본으로 설정됨
        }

        @Test
        @DisplayName("실패: 배송지 없음")
        void 배송지_없음() {
            // given
            Long memberId = 1L;
            Long addressId = 999L;

            given(shippingAddressMapper.findById(addressId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> shippingAddressService.deleteAddress(memberId, addressId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AddressError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void 권한_없음() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long addressId = 1L;
            ShippingAddress address = TestFixture.createShippingAddress(addressId, otherMemberId, false);

            given(shippingAddressMapper.findById(addressId)).willReturn(address);

            // when & then
            assertThatThrownBy(() -> shippingAddressService.deleteAddress(memberId, addressId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AddressError.NOT_OWNER);
        }
    }

    @Nested
    @DisplayName("setDefaultAddress: 기본 배송지 설정")
    class SetDefaultAddress {

        @Test
        @DisplayName("성공: 기본 배송지 설정")
        void 기본_배송지_설정_성공() {
            // given
            Long memberId = 1L;
            Long addressId = 2L;
            ShippingAddress address = TestFixture.createShippingAddress(addressId, memberId, false);
            ShippingAddress updatedAddress = TestFixture.createShippingAddress(addressId, memberId, true);

            given(shippingAddressMapper.findById(addressId)).willReturn(address, updatedAddress);

            // when
            AddressResponse response = shippingAddressService.setDefaultAddress(memberId, addressId);

            // then
            assertThat(response).isNotNull();
            verify(shippingAddressMapper).clearDefaultByMemberId(memberId);
            verify(shippingAddressMapper).setDefault(addressId);
        }

        @Test
        @DisplayName("실패: 배송지 없음")
        void 배송지_없음() {
            // given
            Long memberId = 1L;
            Long addressId = 999L;

            given(shippingAddressMapper.findById(addressId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> shippingAddressService.setDefaultAddress(memberId, addressId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AddressError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void 권한_없음() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long addressId = 1L;
            ShippingAddress address = TestFixture.createShippingAddress(addressId, otherMemberId, true);

            given(shippingAddressMapper.findById(addressId)).willReturn(address);

            // when & then
            assertThatThrownBy(() -> shippingAddressService.setDefaultAddress(memberId, addressId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AddressError.NOT_OWNER);
        }
    }
}
