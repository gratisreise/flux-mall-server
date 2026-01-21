package com.fluxmall.product.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.domain.Product.ProductStatus;
import com.fluxmall.product.dto.request.ProductCreateRequest;
import com.fluxmall.product.dto.request.ProductUpdateRequest;
import com.fluxmall.product.dto.response.ProductResponse;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import com.fluxmall.support.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("getProductDetail: 상품 상세 조회")
    class GetProductDetail {

        @Test
        @DisplayName("성공: 상품 상세 조회")
        void 상품_상세_조회_성공() {
            // given
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "테스트 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);

            // when
            ProductResponse response = productService.getProductDetail(productId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(productId);
        }

        @Test
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long productId = 999L;

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> productService.getProductDetail(productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("createProduct: 상품 등록")
    class CreateProduct {

        @Test
        @DisplayName("성공: 상품 등록")
        void 상품_등록_성공() {
            // given
            Long memberId = 2L;
            ProductCreateRequest request = new ProductCreateRequest(
                    "새 상품",
                    "상품 설명",
                    "ELECTRONICS",
                    15000,
                    50
            );

            // when
            ProductResponse response = productService.createProduct(memberId, request);

            // then
            assertThat(response).isNotNull();
            verify(productMapper).insert(any(Product.class));
        }
    }

    @Nested
    @DisplayName("updateProduct: 상품 수정")
    class UpdateProduct {

        @Test
        @DisplayName("성공: 상품 수정")
        void 상품_수정_성공() {
            // given
            Long memberId = 2L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "기존 상품", 10000, 100);
            ProductUpdateRequest request = new ProductUpdateRequest(
                    "수정된 상품",
                    null,
                    null,
                    15000,
                    null
            );
            Product updatedProduct = TestFixture.createProduct(productId, "수정된 상품", 15000, 100);

            given(productMapper.findById(productId)).willReturn(product, updatedProduct);

            // when
            ProductResponse response = productService.updateProduct(memberId, productId, request);

            // then
            assertThat(response).isNotNull();
            verify(productMapper).update(any(Product.class));
        }

        @Test
        @DisplayName("성공: 재고 0으로 변경 시 품절 처리")
        void 재고_0_품절_처리() {
            // given
            Long memberId = 2L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "기존 상품", 10000, 100);
            ProductUpdateRequest request = new ProductUpdateRequest(
                    null,
                    null,
                    null,
                    null,
                    0  // 재고 0
            );
            Product soldOutProduct = TestFixture.createSoldOutProduct();

            given(productMapper.findById(productId)).willReturn(product, soldOutProduct);

            // when
            ProductResponse response = productService.updateProduct(memberId, productId, request);

            // then
            assertThat(response).isNotNull();
            verify(productMapper).update(any(Product.class));
        }

        @Test
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long memberId = 2L;
            Long productId = 999L;
            ProductUpdateRequest request = new ProductUpdateRequest("수정", null, null, null, null);

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(memberId, productId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void 권한_없음() {
            // given
            Long memberId = 1L;  // 다른 사용자
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "기존 상품", 10000, 100);
            ProductUpdateRequest request = new ProductUpdateRequest("수정", null, null, null, null);

            given(productMapper.findById(productId)).willReturn(product);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(memberId, productId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_OWNER);
        }

        @Test
        @DisplayName("실패: 이미 판매 중단된 상품")
        void 이미_판매_중단() {
            // given
            Long memberId = 2L;
            Long productId = 3L;
            Product discontinuedProduct = TestFixture.createDiscontinuedProduct();
            ProductUpdateRequest request = new ProductUpdateRequest("수정", null, null, null, null);

            given(productMapper.findById(productId)).willReturn(discontinuedProduct);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(memberId, productId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.ALREADY_DISCONTINUED);
        }
    }

    @Nested
    @DisplayName("updateProductStatus: 상품 상태 변경")
    class UpdateProductStatus {

        @Test
        @DisplayName("성공: 상태 변경")
        void 상태_변경_성공() {
            // given
            Long memberId = 2L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "기존 상품", 10000, 100);
            Product soldOutProduct = TestFixture.createSoldOutProduct();

            given(productMapper.findById(productId)).willReturn(product, soldOutProduct);

            // when
            ProductResponse response = productService.updateProductStatus(memberId, productId, ProductStatus.SOLD_OUT);

            // then
            assertThat(response).isNotNull();
            verify(productMapper).updateStatus(productId, ProductStatus.SOLD_OUT.name());
        }

        @Test
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long memberId = 2L;
            Long productId = 999L;

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> productService.updateProductStatus(memberId, productId, ProductStatus.SOLD_OUT))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void 권한_없음() {
            // given
            Long memberId = 1L;  // 다른 사용자
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "기존 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);

            // when & then
            assertThatThrownBy(() -> productService.updateProductStatus(memberId, productId, ProductStatus.SOLD_OUT))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_OWNER);
        }

        @Test
        @DisplayName("실패: 이미 판매 중단된 상품")
        void 이미_판매_중단() {
            // given
            Long memberId = 2L;
            Long productId = 3L;
            Product discontinuedProduct = TestFixture.createDiscontinuedProduct();

            given(productMapper.findById(productId)).willReturn(discontinuedProduct);

            // when & then
            assertThatThrownBy(() -> productService.updateProductStatus(memberId, productId, ProductStatus.ON_SALE))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.ALREADY_DISCONTINUED);
        }
    }

    @Nested
    @DisplayName("deleteProduct: 상품 삭제 (Soft Delete)")
    class DeleteProduct {

        @Test
        @DisplayName("성공: 상품 삭제")
        void 상품_삭제_성공() {
            // given
            Long memberId = 2L;
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "기존 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);

            // when
            productService.deleteProduct(memberId, productId);

            // then
            verify(productMapper).softDelete(productId);
        }

        @Test
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long memberId = 2L;
            Long productId = 999L;

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(memberId, productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void 권한_없음() {
            // given
            Long memberId = 1L;  // 다른 사용자
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "기존 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(memberId, productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_OWNER);
        }

        @Test
        @DisplayName("실패: 이미 판매 중단된 상품")
        void 이미_판매_중단() {
            // given
            Long memberId = 2L;
            Long productId = 3L;
            Product discontinuedProduct = TestFixture.createDiscontinuedProduct();

            given(productMapper.findById(productId)).willReturn(discontinuedProduct);

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(memberId, productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.ALREADY_DISCONTINUED);
        }
    }

    @Nested
    @DisplayName("forceDeleteProduct: 상품 강제 삭제 (Hard Delete)")
    class ForceDeleteProduct {

        @Test
        @DisplayName("성공: 상품 강제 삭제")
        void 강제_삭제_성공() {
            // given
            Long productId = 1L;
            Product product = TestFixture.createProduct(productId, "기존 상품", 10000, 100);

            given(productMapper.findById(productId)).willReturn(product);

            // when
            productService.forceDeleteProduct(productId);

            // then
            verify(productMapper).delete(productId);
        }

        @Test
        @DisplayName("실패: 상품 없음")
        void 상품_없음() {
            // given
            Long productId = 999L;

            given(productMapper.findById(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> productService.forceDeleteProduct(productId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductError.NOT_FOUND);
        }
    }
}
