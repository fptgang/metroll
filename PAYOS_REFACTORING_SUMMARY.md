# PayOS Payment Integration Refactoring

## Summary of Changes

This refactoring eliminates the fallback mock payment system and implements proper error handling for PayOS payment integration.

## 🔧 **Changes Made**

### 1. **New PaymentProcessingException**
- **File**: `shared/src/main/java/com/fpt/metroll/shared/exception/PaymentProcessingException.java`
- **Purpose**: Dedicated exception for payment processing failures
- **HTTP Status**: 502 Bad Gateway (configured in RestExceptionHandler)

### 2. **Refactored PayOSServiceImpl**
- **File**: `order-service/src/main/java/com/fpt/metroll/order/service/impl/PayOSServiceImpl.java`
- **Key Changes**:
  - ❌ **Removed**: Mock payment fallback logic
  - ❌ **Removed**: `createMockPaymentResponse()` method
  - ✅ **Added**: Proper error handling with PaymentProcessingException
  - ✅ **Added**: Improved order code generation logic
  - ✅ **Added**: Order status update to FAILED when payment creation fails

### 3. **Updated OrderServiceImpl**
- **File**: `order-service/src/main/java/com/fpt/metroll/order/service/impl/OrderServiceImpl.java`
- **Key Changes**:
  - ✅ **Improved**: Exception propagation from PayOS service
  - ✅ **Added**: Better error logging

### 4. **Enhanced Exception Handler**
- **File**: `shared/src/main/java/com/fpt/metroll/shared/util/http/RestExceptionHandler.java`
- **Key Changes**:
  - ✅ **Added**: PaymentProcessingException handling with 502 status code

### 5. **New Test Cases**
- **File**: `tests/postman/MetroLL-E2E.postman_collection.json`
- **Key Changes**:
  - ✅ **Added**: PayOS Payment Failure test case
  - ✅ **Added**: Conditional testing logic for proper vs mock behavior

### 6. **Configuration for Testing**
- **File**: `config-server/src/main/resources/config/order-service-payos-failure-test.yml`
- **Purpose**: Configuration file with empty PayOS credentials for testing failure scenarios

## 🚀 **How to Test the Refactored System**

### **Scenario 1: Proper PayOS Configuration (Production-like)**
```bash
# Set environment variables with real PayOS credentials
export PAYOS_CLIENT_ID="your_client_id"
export PAYOS_API_KEY="your_api_key" 
export PAYOS_CHECKSUM_KEY="your_checksum_key"
export PAYOS_WEBHOOK_URL="http://localhost:8080/order/payment/webhook"

# Run the application - PayOS payments will create actual payment links
./gradlew bootRun
```

**Expected Behavior**: 
- ✅ Creates real PayOS payment links
- ✅ Returns actual checkout URLs and QR codes
- ✅ Transaction reference starts with "PAYOS-"

### **Scenario 2: Missing PayOS Configuration (Failure Testing)**
```bash
# Clear PayOS environment variables
unset PAYOS_CLIENT_ID
unset PAYOS_API_KEY
unset PAYOS_CHECKSUM_KEY

# Or set them to empty values
export PAYOS_CLIENT_ID=""
export PAYOS_API_KEY=""
export PAYOS_CHECKSUM_KEY=""

# Run the application
./gradlew bootRun
```

**Expected Behavior**:
- ❌ PayOS payment requests return **502 Bad Gateway**
- ❌ Error message: "PayOS payment gateway is not configured. Please contact administrator."
- ❌ Order status set to FAILED
- ❌ No fallback to mock payment

### **Scenario 3: PayOS API Errors (Network/API Issues)**
**Expected Behavior**:
- ❌ Returns **502 Bad Gateway** with descriptive error message
- ❌ Order status set to FAILED
- ❌ No fallback to mock payment

## 🧪 **Running Tests**

### **Current Test (Shows Mock Behavior)**
```bash
npx newman run tests/postman/MetroLL-E2E.postman_collection.json -e tests/postman/MetroLL-Local.postman_environment.json --folder "Order Service"
```

### **Test with PayOS Failure Configuration**
```bash
# Use the failure test configuration
export SPRING_PROFILES_ACTIVE=payos-failure-test
./gradlew bootRun

# Then run the tests - should show 502 errors for PayOS payments
npx newman run tests/postman/MetroLL-E2E.postman_collection.json -e tests/postman/MetroLL-PayOS-Failure.postman_environment.json
```

## 📋 **API Response Examples**

### **Before (Mock Fallback)**
```json
{
  "id": "order-id",
  "paymentMethod": "PAYOS",
  "status": "PENDING",
  "transactionReference": "MOCK-1750834946389",
  "paymentUrl": "http://localhost:8080/mock-payment/order-id",
  "qrCode": "mock-qr-code"
}
```

### **After (Proper Error)**
```json
{
  "error": "PayOS payment gateway is not configured. Please contact administrator."
}
```
**HTTP Status**: 502 Bad Gateway

### **After (Proper PayOS)**
```json
{
  "id": "order-id", 
  "paymentMethod": "PAYOS",
  "status": "PENDING",
  "transactionReference": "PAYOS-123456789012",
  "paymentUrl": "https://pay.payos.vn/web/...",
  "qrCode": "https://img.vietqr.io/..."
}
```

## ✅ **Benefits of This Refactoring**

1. **🛡️ No More Silent Failures**: PayOS configuration issues are immediately apparent
2. **🚨 Proper Error Codes**: 502 Bad Gateway clearly indicates payment gateway issues
3. **🧹 Cleaner Code**: Removed confusing mock fallback logic
4. **🔍 Better Debugging**: Clear error messages help identify configuration issues
5. **🏗️ Production Ready**: Fails fast when misconfigured rather than providing false success
6. **📊 Accurate Monitoring**: Payment failures are properly tracked and logged

## 🔄 **Migration Notes**

- **Breaking Change**: Environments without proper PayOS configuration will now return 502 instead of mock payments
- **Configuration Required**: Ensure `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, and `PAYOS_CHECKSUM_KEY` are properly set in production
- **Monitoring**: Update monitoring to handle 502 responses for payment gateway issues
- **Fallback Strategy**: If fallback is needed, implement it at the application/UI level, not in the payment service 