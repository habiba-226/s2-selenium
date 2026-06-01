# IoT Smart Monitoring System — QA Automation Suite



## Overview
Selenium WebDriver automation suite for the Smart IoT Monitoring System frontend.
Built with Java, TestNG, Apache POI, and Allure Reports using Page Object Model architecture.




## Test Coverage
| Suite | Tests | Feature |
|---|---|---|
| Entry Point | 7 | F#6 — Navigation & Modals |
| Traffic Dashboard | 13 | F#7 — Table & Charts |
| Filtering & Sorting | 26 | F#8 — Filters, Sort, Pagination |
| Notifications | 17 | F#9 — Bell Center & Toast |
| HTTP Interceptor | 1 | JWT Token Persistence |
| Regression | 68 | Sprint 1 & 2 Re-run |
| **Total** | **138** | |

## Prerequisites
- Java 23
- Maven
- Chrome browser (latest)
- Angular frontend running on `localhost:4200`
- Spring Boot backend running on `localhost:8080`
- MySQL — database `iot_db` with test account `valid@test.com`



## Before Running
1. Ensure both frontend and backend are running
2. Set Traffic Density threshold to 1 in Settings
   (required for notification tests to pass)
3. Reset the database if needed:
```sql
UPDATE users SET failed_attempts=0, lock_time=NULL 
WHERE email='valid@test.com';
```

## Running the Tests
```bash
# Full suite (138 tests)
Run testng.xml via IntelliJ

# Regression only (68 tests)
Run regression.xml via IntelliJ
```

## Allure Report
```bash
# After running the suite
allure serve allure-results
```


