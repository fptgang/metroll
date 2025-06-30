# MetRoll Dashboard Implementation Guide for Frontend Developers

## Overview

This guide provides comprehensive documentation for implementing dashboard features in the MetRoll system. Each microservice now exposes dashboard endpoints that provide real-time statistics and metrics for administrative and operational purposes.

## API Endpoints Summary

### 1. Account Service Dashboard

**Endpoint:** `GET /accounts/dashboard`
**Access:** Admin, Staff
**Purpose:** User management, staff assignments, voucher/discount tracking

### 2. Ticket Service Dashboard

**Endpoint:** `GET /tickets/dashboard`
**Access:** Admin, Staff
**Purpose:** Ticket operations, validation tracking, journey analytics

### 3. Subway Service Dashboard

**Endpoint:** `GET /lines/dashboard`
**Access:** Admin, Staff
**Purpose:** Infrastructure management, line/station/train monitoring

### 4. Order Service Dashboard

**Endpoint:** `GET /orders/dashboard`
**Access:** Admin, Staff
**Purpose:** Revenue tracking, order analytics, payment monitoring

---

## 📊 Dashboard API Specifications

### Account Service Dashboard

```typescript
interface AccountDashboardDto {
  totalAccounts: number;
  accountsByRole: {
    [key: string]: number; // "ADMIN": 5, "STAFF": 20, "CUSTOMER": 150
  };
  activeAccounts: number;
  inactiveAccounts: number;
  staffWithAssignedStation: number;
  staffWithoutAssignedStation: number;
  totalDiscountPackages: number;
  activeDiscountPackages: number;
  totalVouchers: number;
  totalVoucherValue: string; // BigDecimal as string
  lastUpdated: string; // ISO timestamp
}
```

**Example Response:**

```json
{
  "totalAccounts": 175,
  "accountsByRole": {
    "ADMIN": 5,
    "STAFF": 20,
    "CUSTOMER": 150
  },
  "activeAccounts": 170,
  "inactiveAccounts": 5,
  "staffWithAssignedStation": 15,
  "staffWithoutAssignedStation": 5,
  "totalDiscountPackages": 8,
  "activeDiscountPackages": 6,
  "totalVouchers": 250,
  "totalVoucherValue": "50000000.00",
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

### Ticket Service Dashboard

```typescript
interface TicketDashboardDto {
  totalTickets: number;
  ticketsByStatus: {
    [key: string]: number; // "VALID": 1500, "USED": 8500, "EXPIRED": 200
  };
  ticketsByType: {
    [key: string]: number; // "P2P": 7500, "TIMED": 2700
  };
  totalValidations: number;
  validationsByType: {
    [key: string]: number; // "ENTRY": 5000, "EXIT": 4800
  };
  todayValidations: number;
  totalP2PJourneys: number;
  validationsLast7Days: {
    [date: string]: number; // "2024-01-15": 150, "2024-01-14": 200
  };
  lastUpdated: string;
}
```

**Example Response:**

```json
{
  "totalTickets": 10200,
  "ticketsByStatus": {
    "VALID": 1500,
    "USED": 8500,
    "EXPIRED": 200
  },
  "ticketsByType": {
    "P2P": 7500,
    "TIMED": 2700
  },
  "totalValidations": 9800,
  "validationsByType": {
    "ENTRY": 5000,
    "EXIT": 4800
  },
  "todayValidations": 320,
  "totalP2PJourneys": 45,
  "validationsLast7Days": {
    "2024-01-15": 320,
    "2024-01-14": 280,
    "2024-01-13": 250,
    "2024-01-12": 290,
    "2024-01-11": 310,
    "2024-01-10": 275,
    "2024-01-09": 195
  },
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

### Subway Service Dashboard

```typescript
interface SubwayDashboardDto {
  totalStations: number;
  totalMetroLines: number;
  totalTrains: number;
  stationsByMetroLine: {
    [lineName: string]: number; // "Red Line": 15, "Blue Line": 12
  };
  trainsByMetroLine: {
    [lineName: string]: number; // "Red Line": 8, "Blue Line": 6
  };
  averageStationsPerLine: number;
  averageTrainsPerLine: number;
  lastUpdated: string;
}
```

**Example Response:**

```json
{
  "totalStations": 45,
  "totalMetroLines": 3,
  "totalTrains": 18,
  "stationsByMetroLine": {
    "Red Line": 18,
    "Blue Line": 15,
    "Green Line": 12
  },
  "trainsByMetroLine": {
    "Red Line": 8,
    "Blue Line": 6,
    "Green Line": 4
  },
  "averageStationsPerLine": 15.0,
  "averageTrainsPerLine": 6.0,
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

### Order Service Dashboard

```typescript
interface OrderDashboardDto {
  totalOrders: number;
  ordersByStatus: {
    [key: string]: number; // "COMPLETED": 8500, "PENDING": 50, "FAILED": 150
  };
  totalRevenue: string; // BigDecimal as string
  todayRevenue: string;
  weeklyRevenue: string;
  monthlyRevenue: string;
  totalOrderDetails: number;
  averageOrderValue: number;
  todayOrders: number;
  revenueLast7Days: {
    [date: string]: string; // "2024-01-15": "15000000.00"
  };
  lastUpdated: string;
}
```

**Example Response:**

```json
{
  "totalOrders": 8700,
  "ordersByStatus": {
    "COMPLETED": 8500,
    "PENDING": 50,
    "FAILED": 150
  },
  "totalRevenue": "425000000.00",
  "todayRevenue": "15000000.00",
  "weeklyRevenue": "95000000.00",
  "monthlyRevenue": "380000000.00",
  "totalOrderDetails": 12500,
  "averageOrderValue": 50000.0,
  "todayOrders": 300,
  "revenueLast7Days": {
    "2024-01-15": "15000000.00",
    "2024-01-14": "14200000.00",
    "2024-01-13": "12800000.00",
    "2024-01-12": "13500000.00",
    "2024-01-11": "14800000.00",
    "2024-01-10": "13200000.00",
    "2024-01-09": "11500000.00"
  },
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

---

## 🔐 Authentication & Authorization

All dashboard endpoints require authentication. Include the Bearer token in request headers:

```typescript
const headers = {
  Authorization: `Bearer ${authToken}`,
  "Content-Type": "application/json",
};
```

**Access Control:**

- **ADMIN**: Full access to all dashboards
- **STAFF**: Read-only access to all dashboards
- **CUSTOMER**: No dashboard access

---

## 🚀 Implementation Examples

### React/TypeScript Implementation

#### 1. Service Layer

```typescript
// services/dashboardService.ts
import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

class DashboardService {
  private getHeaders() {
    const token = localStorage.getItem("authToken");
    return {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    };
  }

  async getAccountDashboard(): Promise<AccountDashboardDto> {
    const response = await axios.get(
      `${API_BASE_URL}/account/accounts/dashboard`,
      { headers: this.getHeaders() }
    );
    return response.data;
  }

  async getTicketDashboard(): Promise<TicketDashboardDto> {
    const response = await axios.get(
      `${API_BASE_URL}/ticket/tickets/dashboard`,
      { headers: this.getHeaders() }
    );
    return response.data;
  }

  async getSubwayDashboard(): Promise<SubwayDashboardDto> {
    const response = await axios.get(`${API_BASE_URL}/subway/lines/dashboard`, {
      headers: this.getHeaders(),
    });
    return response.data;
  }

  async getOrderDashboard(): Promise<OrderDashboardDto> {
    const response = await axios.get(`${API_BASE_URL}/order/orders/dashboard`, {
      headers: this.getHeaders(),
    });
    return response.data;
  }

  async getAllDashboards(): Promise<{
    accounts: AccountDashboardDto;
    tickets: TicketDashboardDto;
    subway: SubwayDashboardDto;
    orders: OrderDashboardDto;
  }> {
    const [accounts, tickets, subway, orders] = await Promise.all([
      this.getAccountDashboard(),
      this.getTicketDashboard(),
      this.getSubwayDashboard(),
      this.getOrderDashboard(),
    ]);

    return { accounts, tickets, subway, orders };
  }
}

export const dashboardService = new DashboardService();
```

#### 2. React Hooks

```typescript
// hooks/useDashboard.ts
import { useState, useEffect } from "react";
import { dashboardService } from "../services/dashboardService";

export const useDashboard = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDashboards = async () => {
    try {
      setLoading(true);
      const dashboards = await dashboardService.getAllDashboards();
      setData(dashboards);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboards();

    // Auto-refresh every 5 minutes
    const interval = setInterval(fetchDashboards, 5 * 60 * 1000);
    return () => clearInterval(interval);
  }, []);

  return { data, loading, error, refetch: fetchDashboards };
};
```

#### 3. Dashboard Components

```typescript
// components/Dashboard/AccountDashboard.tsx
import React from "react";
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

interface Props {
  data: AccountDashboardDto;
}

export const AccountDashboard: React.FC<Props> = ({ data }) => {
  const roleData = Object.entries(data.accountsByRole).map(([role, count]) => ({
    name: role,
    value: count,
  }));

  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042"];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {/* Summary Cards */}
      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-2">Total Accounts</h3>
        <p className="text-3xl font-bold text-blue-600">{data.totalAccounts}</p>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-2">Active Accounts</h3>
        <p className="text-3xl font-bold text-green-600">
          {data.activeAccounts}
        </p>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-2">Staff Assigned</h3>
        <p className="text-3xl font-bold text-purple-600">
          {data.staffWithAssignedStation}
        </p>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-2">Total Vouchers</h3>
        <p className="text-3xl font-bold text-orange-600">
          {data.totalVouchers}
        </p>
      </div>

      {/* Charts */}
      <div className="col-span-full md:col-span-2 bg-white p-6 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-4">Accounts by Role</h3>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={roleData}
              cx="50%"
              cy="50%"
              outerRadius={100}
              fill="#8884d8"
              dataKey="value"
              label={({ name, value }) => `${name}: ${value}`}
            >
              {roleData.map((entry, index) => (
                <Cell
                  key={`cell-${index}`}
                  fill={COLORS[index % COLORS.length]}
                />
              ))}
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div className="col-span-full md:col-span-2 bg-white p-6 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-4">Staff Assignment Status</h3>
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <span>With Assigned Station</span>
            <span className="font-bold text-green-600">
              {data.staffWithAssignedStation}
            </span>
          </div>
          <div className="flex justify-between items-center">
            <span>Without Assigned Station</span>
            <span className="font-bold text-red-600">
              {data.staffWithoutAssignedStation}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
```

#### 4. Main Dashboard Page

```typescript
// pages/Dashboard.tsx
import React from "react";
import { useDashboard } from "../hooks/useDashboard";
import { AccountDashboard } from "../components/Dashboard/AccountDashboard";
import { TicketDashboard } from "../components/Dashboard/TicketDashboard";
import { SubwayDashboard } from "../components/Dashboard/SubwayDashboard";
import { OrderDashboard } from "../components/Dashboard/OrderDashboard";

export const Dashboard: React.FC = () => {
  const { data, loading, error, refetch } = useDashboard();

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        <p>Error loading dashboard: {error}</p>
        <button
          onClick={refetch}
          className="mt-2 bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">MetRoll Dashboard</h1>
        <button
          onClick={refetch}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Refresh
        </button>
      </div>

      <div className="space-y-8">
        <section>
          <h2 className="text-2xl font-semibold mb-4">Account Management</h2>
          <AccountDashboard data={data.accounts} />
        </section>

        <section>
          <h2 className="text-2xl font-semibold mb-4">Ticket Operations</h2>
          <TicketDashboard data={data.tickets} />
        </section>

        <section>
          <h2 className="text-2xl font-semibold mb-4">Subway Infrastructure</h2>
          <SubwayDashboard data={data.subway} />
        </section>

        <section>
          <h2 className="text-2xl font-semibold mb-4">Order & Revenue</h2>
          <OrderDashboard data={data.orders} />
        </section>
      </div>

      <div className="mt-8 text-center text-gray-500">
        <p>
          Last updated: {new Date(data.accounts.lastUpdated).toLocaleString()}
        </p>
      </div>
    </div>
  );
};
```

---

## 📱 Mobile Implementation (React Native)

```typescript
// services/DashboardService.ts
import AsyncStorage from "@react-native-async-storage/async-storage";

class MobileDashboardService {
  private async getAuthHeaders() {
    const token = await AsyncStorage.getItem("authToken");
    return {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    };
  }

  async fetchDashboards() {
    const headers = await this.getAuthHeaders();

    try {
      const responses = await Promise.all([
        fetch(`${API_BASE_URL}/account/accounts/dashboard`, { headers }),
        fetch(`${API_BASE_URL}/ticket/tickets/dashboard`, { headers }),
        fetch(`${API_BASE_URL}/subway/lines/dashboard`, { headers }),
        fetch(`${API_BASE_URL}/order/orders/dashboard`, { headers }),
      ]);

      const [accounts, tickets, subway, orders] = await Promise.all(
        responses.map((res) => res.json())
      );

      return { accounts, tickets, subway, orders };
    } catch (error) {
      throw new Error(`Failed to fetch dashboards: ${error.message}`);
    }
  }
}
```

---

## 🎨 Chart Library Recommendations

### For React Web Applications:

1. **Recharts** - Simple, composable charts

   ```bash
   npm install recharts
   ```

2. **Chart.js with react-chartjs-2** - Feature-rich charting

   ```bash
   npm install chart.js react-chartjs-2
   ```

3. **Victory** - Modular charting components
   ```bash
   npm install victory
   ```

### For Vue.js Applications:

1. **Chart.js with vue-chartjs**

   ```bash
   npm install chart.js vue-chartjs
   ```

2. **ApexCharts with vue-apexcharts**
   ```bash
   npm install apexcharts vue-apexcharts
   ```

### For Angular Applications:

1. **ng2-charts**

   ```bash
   npm install ng2-charts chart.js
   ```

2. **ngx-charts**
   ```bash
   npm install @swimlane/ngx-charts
   ```

---

## 🔄 Real-time Updates

### WebSocket Implementation (Optional)

```typescript
// services/websocketService.ts
class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectInterval: number = 5000;

  connect(onDashboardUpdate: (data: any) => void) {
    this.ws = new WebSocket("ws://localhost:8080/ws/dashboard");

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      onDashboardUpdate(data);
    };

    this.ws.onclose = () => {
      setTimeout(() => this.connect(onDashboardUpdate), this.reconnectInterval);
    };
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}
```

### Polling Strategy

```typescript
// hooks/usePolling.ts
export const usePolling = (
  fetchFn: () => Promise<any>,
  interval: number = 30000
) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const poll = async () => {
      try {
        const result = await fetchFn();
        setData(result);
      } catch (error) {
        console.error("Polling error:", error);
      } finally {
        setLoading(false);
      }
    };

    poll(); // Initial fetch
    const intervalId = setInterval(poll, interval);

    return () => clearInterval(intervalId);
  }, [fetchFn, interval]);

  return { data, loading };
};
```

---

## 🚦 Error Handling Best Practices

### 1. HTTP Error Handling

```typescript
const handleApiError = (error: any) => {
  if (error.response?.status === 401) {
    // Redirect to login
    window.location.href = "/login";
  } else if (error.response?.status === 403) {
    // Show permission denied message
    toast.error("You do not have permission to view this dashboard");
  } else if (error.response?.status >= 500) {
    // Server error
    toast.error("Server error. Please try again later.");
  } else {
    // Generic error
    toast.error("An unexpected error occurred");
  }
};
```

### 2. Loading States

```typescript
const LoadingSpinner = () => (
  <div className="flex justify-center items-center h-40">
    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
  </div>
);

const DashboardCard = ({ title, data, loading }) => (
  <div className="bg-white p-6 rounded-lg shadow">
    <h3 className="text-lg font-semibold mb-2">{title}</h3>
    {loading ? <LoadingSpinner /> : <div>{data}</div>}
  </div>
);
```

---

## 🎯 Performance Optimization

### 1. Data Caching

```typescript
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

class CachedDashboardService {
  private cache = new Map();

  async getDashboardData(type: string) {
    const cacheKey = `dashboard_${type}`;
    const cached = this.cache.get(cacheKey);

    if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
      return cached.data;
    }

    const data = await this.fetchDashboardData(type);
    this.cache.set(cacheKey, { data, timestamp: Date.now() });
    return data;
  }
}
```

### 2. Component Memoization

```typescript
const DashboardChart = React.memo(
  ({ data, type }) => {
    // Chart rendering logic
  },
  (prevProps, nextProps) => {
    return JSON.stringify(prevProps.data) === JSON.stringify(nextProps.data);
  }
);
```

---

## 🎨 UI/UX Recommendations

### 1. Color Scheme

```css
:root {
  --primary-blue: #3b82f6;
  --success-green: #10b981;
  --warning-yellow: #f59e0b;
  --danger-red: #ef4444;
  --neutral-gray: #6b7280;
  --background: #f9fafb;
  --surface: #ffffff;
}
```

### 2. Responsive Design

```css
/* Mobile First Approach */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

@media (min-width: 768px) {
  .dashboard-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
```

### 3. Accessibility

```typescript
const AccessibleChart = ({ data, ariaLabel }) => (
  <div role="img" aria-label={ariaLabel}>
    <Chart data={data} />
    <div className="sr-only">
      {/* Screen reader friendly data summary */}
      {data.map((item, index) => (
        <span key={index}>
          {item.label}: {item.value}
        </span>
      ))}
    </div>
  </div>
);
```

---

## 🧪 Testing

### Unit Tests

```typescript
// __tests__/dashboardService.test.ts
import { dashboardService } from "../services/dashboardService";

describe("DashboardService", () => {
  it("should fetch account dashboard data", async () => {
    const mockData = { totalAccounts: 100 };
    jest.spyOn(global, "fetch").mockResolvedValue({
      json: () => Promise.resolve(mockData),
    });

    const result = await dashboardService.getAccountDashboard();
    expect(result).toEqual(mockData);
  });
});
```

### Integration Tests

```typescript
// __tests__/Dashboard.integration.test.tsx
import { render, screen, waitFor } from "@testing-library/react";
import { Dashboard } from "../pages/Dashboard";

test("displays dashboard data when loaded", async () => {
  render(<Dashboard />);

  await waitFor(() => {
    expect(screen.getByText("Total Accounts")).toBeInTheDocument();
    expect(screen.getByText("100")).toBeInTheDocument();
  });
});
```

---

## 🚀 Deployment Considerations

### 1. Environment Configuration

```typescript
// config/environment.ts
export const config = {
  apiBaseUrl: process.env.REACT_APP_API_BASE_URL || "http://localhost:8080",
  wsUrl: process.env.REACT_APP_WS_URL || "ws://localhost:8080/ws",
  refreshInterval: parseInt(process.env.REACT_APP_REFRESH_INTERVAL || "300000"), // 5 min
  cacheTimeout: parseInt(process.env.REACT_APP_CACHE_TIMEOUT || "300000"), // 5 min
};
```

### 2. Build Optimization

```json
// package.json
{
  "scripts": {
    "build": "react-scripts build",
    "build:analyze": "npm run build && npx bundle-analyzer build/static/js/*.js"
  }
}
```

This comprehensive guide provides everything needed to implement a fully functional dashboard system for the MetRoll application. The modular approach allows for incremental development and easy maintenance.
