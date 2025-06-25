#!/bin/bash

echo "🚀 MetroLL Saga Pattern Test Suite"
echo "=================================="
echo ""

# Check if Newman is installed
if ! command -v newman &> /dev/null; then
    echo "❌ Newman is not installed. Installing..."
    npm install -g newman
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}📋 Starting Saga Test Suite...${NC}"
echo ""

# Set test environment
export TEST_ENV="saga-test"
export BASE_URL="http://localhost:8080"

# Create results directory
mkdir -p ./results

# Run the saga tests
echo -e "${YELLOW}🔄 Running Saga Pattern Tests...${NC}"
newman run MetroLL-Saga-Tests.postman_collection.json \
    -e MetroLL-Saga-Environment.postman_environment.json \
    --reporters cli,json,html \
    --reporter-json-export ./results/saga-test-results.json \
    --reporter-html-export ./results/saga-test-report.html \
    --timeout-request 30000 \
    --delay-request 1000 \
    --color on

# Check if tests passed
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Saga tests completed successfully!${NC}"
    echo ""
    echo -e "${BLUE}📊 Test Results Summary:${NC}"
    echo "• Test report: ./results/saga-test-report.html"
    echo "• JSON results: ./results/saga-test-results.json"
    echo ""
    
    # Performance analysis
    echo -e "${BLUE}⚡ Performance Analysis:${NC}"
    echo "• Saga initiation should be < 500ms"
    echo "• Status checks should be < 200ms"
    echo "• Concurrent processing validation"
    echo ""
    
    # Saga pattern validation
    echo -e "${BLUE}🔄 Saga Pattern Validation:${NC}"
    echo "✅ Asynchronous processing (202 Accepted)"
    echo "✅ State tracking and monitoring"
    echo "✅ Failure handling and compensation"
    echo "✅ Concurrent saga execution"
    echo "✅ Edge case handling"
    echo ""
    
    echo -e "${GREEN}🎉 All saga pattern benefits validated!${NC}"
    
else
    echo ""
    echo -e "${RED}❌ Saga tests failed!${NC}"
    echo ""
    echo -e "${YELLOW}🔍 Troubleshooting:${NC}"
    echo "• Ensure MetroLL services are running on localhost:8080"
    echo "• Check if RabbitMQ is properly configured"
    echo "• Verify JWT authentication is working"
    echo "• Ensure test data is available (P2P journeys)"
    echo ""
    echo "Check the detailed report at: ./results/saga-test-report.html"
    exit 1
fi

echo ""
echo -e "${BLUE}📖 Next Steps:${NC}"
echo "1. Review the HTML report for detailed test analysis"
echo "2. Monitor saga execution in your application logs"
echo "3. Check RabbitMQ message queues for saga events"
echo "4. Validate compensation logic in failure scenarios"
echo ""
echo -e "${YELLOW}💡 Pro Tips:${NC}"
echo "• Run these tests after any saga-related code changes"
echo "• Use concurrent tests to validate system under load"
echo "• Monitor saga timeouts and failure patterns"
echo "• Check database state consistency after compensations" 