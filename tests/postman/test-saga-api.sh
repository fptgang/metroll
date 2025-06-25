#!/bin/bash

echo "🧪 Quick Saga API Test"
echo "====================="
echo ""

BASE_URL="https://f606-58-187-123-8.ngrok-free.app"

# Test 1: Check if API Gateway is accessible
echo "1️⃣ Testing API Gateway connectivity..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null || echo "000")
if [ "$response" = "200" ]; then
    echo "✅ API Gateway is accessible"
else
    echo "❌ API Gateway not accessible (HTTP $response)"
    echo "   Make sure MetroLL services are running on localhost:8080"
    exit 1
fi

echo ""

# Test 2: Test saga checkout endpoint (without auth for quick check)
echo "2️⃣ Testing Saga Checkout endpoint structure..."
response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d '{"paymentMethod":"CASH","items":[{"ticketType":"P2P","p2pJourneyId":"test","quantity":1}]}' \
    -w "%{http_code}" \
    "$BASE_URL/orders/checkout-saga" 2>/dev/null || echo "000")

if [ "$response" = "401" ]; then
    echo "✅ Saga endpoint exists (requires authentication as expected)"
elif [ "$response" = "202" ]; then
    echo "✅ Saga endpoint working (unexpected but good!)"
else
    echo "⚠️ Saga endpoint returned HTTP $response"
    echo "   This might be expected depending on your auth setup"
fi

echo ""

# Test 3: Test saga status endpoint
echo "3️⃣ Testing Saga Status endpoint structure..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/orders/saga/test-id/status" 2>/dev/null || echo "000")
if [ "$response" = "401" ] || [ "$response" = "200" ] || [ "$response" = "404" ]; then
    echo "✅ Saga status endpoint exists"
else
    echo "⚠️ Saga status endpoint returned HTTP $response"
fi

echo ""
echo "📋 Postman Collection Test Instructions:"
echo "======================================="
echo ""
echo "To run the comprehensive saga tests:"
echo ""
echo "1. Install Newman (Postman CLI):"
echo "   npm install -g newman"
echo ""
echo "2. Start your MetroLL services:"
echo "   docker-compose up -d"
echo ""
echo "3. Run the saga test suite:"
echo "   cd tests/postman"
echo "   ./run-saga-tests.sh"
echo ""
echo "   Or manually with Newman:"
echo "   newman run MetroLL-Saga-Tests.postman_collection.json \\"
echo "          -e MetroLL-Saga-Environment.postman_environment.json"
echo ""
echo "📖 Test Coverage:"
echo "• ✅ Saga initiation and async processing"
echo "• 🔄 Saga state tracking and monitoring"
echo "• ❌ Failure scenarios and compensation"
echo "• 🚀 Performance benchmarking (< 500ms response)"
echo "• 🔀 Concurrent saga execution"
echo "• 🛡️ Edge cases (invalid IDs, non-existent sagas)"
echo ""
echo "🎯 Expected Results:"
echo "• All saga requests should return 202 Accepted"
echo "• Status endpoints should show saga progression"
echo "• Failures should trigger compensation flows"
echo "• Response times should be under 500ms"
echo ""
echo "🔍 Monitoring:"
echo "• Check RabbitMQ queues for saga events"
echo "• Monitor application logs for saga processing"
echo "• Verify database state after compensations" 