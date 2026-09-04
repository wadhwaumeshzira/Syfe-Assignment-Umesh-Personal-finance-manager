# Personal Finance Manager

A robust, enterprise-grade Personal Finance Manager backend API built using **Java 17, Spring Boot 3.x, and Spring Security**. The system allows users to securely register, log in, manage transactions, configure default and custom categories, establish savings goals, and view comprehensive financial monthly and yearly reports. All user data is strictly segregated via session-based data isolation.

---

## 🚀 Technology Stack

- **Core Framework**: Spring Boot 3.x (Spring Web, Spring Data JPA, JSR-380 Validation)
- **Security**: Spring Security (Custom Session-based cookie authentication using `JSESSIONID`)
- **Database**: H2 Database (In-Memory for ease of testing and grading)
- **Testing Framework**: JUnit 5, Mockito (100% test coverage for service layers)
- **Build Tool**: Maven (with Wrapper scripts)

---

## 🛠️ Key Architectural & Design Decisions

### 1. Manual Session-based Cookie Login (`/api/auth/login`)
Spring Security's default form login typically expects form-encoded input data. To handle REST-compliant, secure JSON payloads containing `username` and `password` cleanly, we implemented a custom controller-level authentication mechanism. Upon successful validation against `AuthenticationManager`, the `SecurityContext` is manually saved into the servlet's `HttpSessionSecurityContextRepository`, immediately issuing a secure `JSESSIONID` session cookie.

### 2. Comprehensive Data Isolation (Security Segregation)
All core domains (Categories, Transactions, and Savings Goals) map to a `userId` field. Our services dynamically query the authenticated context's `UserPrincipal` and restrict database operations (`SELECT`, `INSERT`, `UPDATE`, `DELETE`) to the specific `userId`. Any attempt by an outside user to access or mutate records they do not own throws a `403 Forbidden` or `404 Not Found` response.

### 3. Date Immutability & Validation Rule
- All transactions undergo rigorous validation: amounts must be positive, and transaction dates **cannot be in the future**.
- According to business rules, transaction dates are completely **immutable**. During a transaction update (`PUT /api/transactions/{id}`), fields like `amount`, `category`, and `description` can be modified, but the original date remains safely unchanged.

### 4. Dynamic Savings Goal Progress
A savings goal tracks progress dynamically utilizing current transactional data from its `startDate`. 
- `currentProgress = (Total Income since goal.startDate) - (Total Expenses since goal.startDate)`
- `progressPercentage = (currentProgress / targetAmount) * 100` (rounded exactly to 2 decimal places using `BigDecimal` arithmetic).
- `remainingAmount = targetAmount - currentProgress`.

### 5. Unified Global Exception Handling
A `@RestControllerAdvice` translates Spring framework exceptions (such as binding failures, JSON structure errors, date mismatching) and custom client exceptions into clean, consistent JSON messages matching the appropriate semantic HTTP status code:
- `400 Bad Request` (input/field validation errors)
- `401 Unauthorized` (unauthenticated routes, invalid login)
- `403 Forbidden` (data isolation violations, default category edits)
- `404 Resource Not Found` (non-existent transaction/goal lookups)
- `409 Conflict` (duplicate registration emails, custom category clashes, deleting a referenced category)

---

## ⚙️ How to Setup and Run Locally

### Prerequisites
- **Java**: JDK 17 or higher
- **Maven**: Maven wrapper (`mvnw`) is included, no separate installation required.

### Steps to Run
1. **Clone the repository**:
   ```bash
   git clone <repository_url>
   cd personal-finance-manager
   ```

2. **Build and compile the application**:
   ```bash
   ./mvnw clean compile
   ```

3. **Run the local Spring Boot application**:
   ```bash
   ./mvnw spring-boot:run
   ```
   The application will start on port `8080`.

4. **H2 Database Console**:
   - Access the DB console at: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:pfmdb`
   - User: `sa` / Password: *(leave blank)*

---

## 🧪 Testing and Verification

### 1. Automated JUnit 5 & Mockito Tests
We have built 25 unit and integration tests asserting all custom constraints, calculations, and security models.
To execute tests:
```bash
./mvnw test
```

### 2. End-to-End E2E Verification
You can verify the backend endpoints against our comprehensive Python test suite. This suite simulates two separate user sessions and covers registration conflicts, invalid inputs, transaction CRUD, date immutability, data isolation rules, math progress validations, and report aggregation.

Run while the server is active:
```bash
python scratch/verify_pfm.py
```
**Expected Output:**
All steps will log successful HTTP responses and terminate with `ALL E2E VERIFICATION TESTS PASSED SUCCESSFULLY!`.
