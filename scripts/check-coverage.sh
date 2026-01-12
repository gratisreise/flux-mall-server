##!/bin/bash
#
## Jacoco XML 리포트 경로 (프로젝트 구조에 따라 수정 필요)
#REPORT_PATH="build/reports/jacoco/test/jacocoTestReport.xml"
#THRESHOLD=80
#
## 1. 리포트 파일 존재 확인
#if [ ! -f "$REPORT_PATH" ]; then
#    echo "❌ Jacoco report not found at $REPORT_PATH"
#    exit 1
#fi
#
## 2. XML에서 'instruction' 라인의 커버리지 계산 (간단한 파싱)
## missed와 covered 값을 가져와서 계산
#MISSED=$(grep -oP '<counter type="INSTRUCTION" missed="\K[^"]+' $REPORT_PATH | head -1)
#COVERED=$(grep -oP '<counter type="INSTRUCTION" missed="[^"]+" covered="\K[^"]+' $REPORT_PATH | head -1)
#
#TOTAL=$((MISSED + COVERED))
#COVERAGE=$(echo "scale=2; $COVERED / $TOTAL * 100" | bc | cut -d. -f1)
#
#echo "📊 Current Test Coverage: $COVERAGE%"
#
## 3. 기준치와 비교
#if [ "$COVERAGE" -lt "$THRESHOLD" ]; then
#    echo "❌ Coverage is below $THRESHOLD%. Build failed."
#    exit 1
#else
#    echo "✅ Coverage check passed!"
#    exit 0
#fi