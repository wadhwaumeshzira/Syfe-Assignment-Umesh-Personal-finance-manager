import requests
import sys

BASE_URL = "http://localhost:8080"

session_a = requests.Session()
session_b = requests.Session()

def run_tests():
    print("====================================================")
    print("      STARTING E2E VERIFICATION FOR PFM API        ")
    print("====================================================")

    # 1. Registration tests
    print("\n--- 1. User Management & Authentication ---")
    user_a = {
        "username": "user_a@syfe.com",
        "password": "password123",
        "fullName": "User Alpha",
        "phoneNumber": "+1234567890"
    }
    user_b = {
        "username": "user_b@syfe.com",
        "password": "password456",
        "fullName": "User Beta",
        "phoneNumber": "+0987654321"
    }

    # Register A
    r = requests.post(f"{BASE_URL}/api/auth/register", json=user_a)
    print(f"Register User A: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 201, "User A registration failed"
    assert r.json()["userId"] is not None
    assert "registered successfully" in r.json()["message"]

    # Register A duplicate (Conflict 409)
    r = requests.post(f"{BASE_URL}/api/auth/register", json=user_a)
    print(f"Register Duplicate User A: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 409, "Expected 409 Conflict for duplicate registration"

    # Register B
    r = requests.post(f"{BASE_URL}/api/auth/register", json=user_b)
    print(f"Register User B: Status = {r.status_code}")
    assert r.status_code == 201

    # Unauthorized access check
    r = requests.get(f"{BASE_URL}/api/categories")
    print(f"Access /api/categories unauthenticated: Status = {r.status_code}")
    assert r.status_code == 401

    # Login with bad credentials (401)
    r = session_a.post(f"{BASE_URL}/api/auth/login", json={"username": "user_a@syfe.com", "password": "wrongpassword"})
    print(f"Login User A (wrong password): Status = {r.status_code}")
    assert r.status_code == 401

    # Login A (200)
    r = session_a.post(f"{BASE_URL}/api/auth/login", json={"username": "user_a@syfe.com", "password": "password123"})
    print(f"Login User A: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 200
    assert "Login successful" in r.json()["message"]

    # Login B (200)
    r = session_b.post(f"{BASE_URL}/api/auth/login", json={"username": "user_b@syfe.com", "password": "password456"})
    print(f"Login User B: Status = {r.status_code}")
    assert r.status_code == 200

    # 2. Category Management
    print("\n--- 2. Category Management ---")
    r = session_a.get(f"{BASE_URL}/api/categories")
    print(f"Get Categories for User A: Status = {r.status_code}")
    assert r.status_code == 200
    categories = r.json()["categories"]
    cat_names = [c["name"] for c in categories]
    print(f"Categories in DB: {cat_names}")
    assert "Salary" in cat_names
    assert "Food" in cat_names
    assert "Rent" in cat_names
    
    # Create custom category
    custom_cat = {"name": "Freelance", "type": "INCOME"}
    r = session_a.post(f"{BASE_URL}/api/categories", json=custom_cat)
    print(f"Create Custom Category A: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 201
    assert r.json()["isCustom"] is True
    assert r.json()["name"] == "Freelance"

    # Create duplicate custom category (Conflict 409)
    r = session_a.post(f"{BASE_URL}/api/categories", json=custom_cat)
    print(f"Create Duplicate Custom Category: Status = {r.status_code}")
    assert r.status_code == 409

    # Verify B doesn't see A's custom category
    r = session_b.get(f"{BASE_URL}/api/categories")
    cat_names_b = [c["name"] for c in r.json()["categories"]]
    assert "Freelance" not in cat_names_b, "User B should not see User A's custom category"

    # 3. Transaction Management
    print("\n--- 3. Transaction Management ---")
    tx1 = {
        "amount": 5000.00,
        "date": "2026-05-01",
        "category": "Salary",
        "description": "May Salary"
    }
    tx2 = {
        "amount": 1200.00,
        "date": "2026-05-02",
        "category": "Rent",
        "description": "Apartment Rent"
    }
    tx3 = {
        "amount": 150.50,
        "date": "2026-05-03",
        "category": "Food",
        "description": "Groceries"
    }

    # Create transactions for A
    r = session_a.post(f"{BASE_URL}/api/transactions", json=tx1)
    print(f"Create Transaction 1: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 201
    tx1_id = r.json()["id"]
    assert r.json()["type"] == "INCOME"

    r = session_a.post(f"{BASE_URL}/api/transactions", json=tx2)
    tx2_id = r.json()["id"]
    assert r.json()["type"] == "EXPENSE"

    r = session_a.post(f"{BASE_URL}/api/transactions", json=tx3)
    tx3_id = r.json()["id"]

    # Invalid amount validation (400)
    r = session_a.post(f"{BASE_URL}/api/transactions", json={"amount": -5.0, "date": "2026-05-01", "category": "Food"})
    print(f"Create Transaction Negative Amount: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 400

    # Future date validation (400)
    r = session_a.post(f"{BASE_URL}/api/transactions", json={"amount": 10.0, "date": "2030-05-01", "category": "Food"})
    print(f"Create Transaction Future Date: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 400

    # Get transactions & verify sorted newest first
    r = session_a.get(f"{BASE_URL}/api/transactions")
    print(f"Get Transactions A: Status = {r.status_code}")
    txs = r.json()["transactions"]
    assert len(txs) == 3
    assert txs[0]["id"] == tx3_id

    # Filter transactions by category and date range
    r = session_a.get(f"{BASE_URL}/api/transactions?startDate=2026-05-01&endDate=2026-05-02&category=Salary")
    print(f"Filter Transactions: Status = {r.status_code}")
    filtered = r.json()["transactions"]
    assert len(filtered) == 1
    assert filtered[0]["id"] == tx1_id

    # Update transaction (date immutable)
    r = session_a.put(f"{BASE_URL}/api/transactions/{tx1_id}", json={"amount": 5500.00, "description": "Updated May Salary"})
    print(f"Update Transaction 1: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 200
    assert r.json()["amount"] == 5500.00
    assert r.json()["date"] == "2026-05-01"

    # 4. Data Isolation verification
    print("\n--- 4. Data Isolation Verification ---")
    r = session_b.get(f"{BASE_URL}/api/transactions")
    print(f"Get Transactions B (should be empty list): Status = {r.status_code}, Count = {len(r.json()['transactions'])}")
    assert len(r.json()["transactions"]) == 0

    r = session_b.put(f"{BASE_URL}/api/transactions/{tx1_id}", json={"amount": 1.00})
    print(f"User B updates User A's Transaction: Status = {r.status_code}")
    assert r.status_code == 403

    r = session_b.delete(f"{BASE_URL}/api/transactions/{tx1_id}")
    print(f"User B deletes User A's Transaction: Status = {r.status_code}")
    assert r.status_code == 403

    # 5. Savings Goals
    print("\n--- 5. Savings Goals ---")
    goal = {
        "goalName": "Emergency Fund",
        "targetAmount": 5000.00,
        "targetDate": "2027-01-01",
        "startDate": "2026-05-01"
    }
    r = session_a.post(f"{BASE_URL}/api/goals", json=goal)
    print(f"Create Savings Goal: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 201
    goal_id = r.json()["id"]
    
    assert r.json()["currentProgress"] == 4149.50
    assert r.json()["progressPercentage"] == 82.99
    assert r.json()["remainingAmount"] == 850.50

    # Update goal
    r = session_a.put(f"{BASE_URL}/api/goals/{goal_id}", json={"targetAmount": 6000.00})
    print(f"Update Savings Goal: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 200
    assert r.json()["targetAmount"] == 6000.00
    assert r.json()["progressPercentage"] == 69.16
    assert r.json()["remainingAmount"] == 1850.50

    # B tries to get A's goal (403 Forbidden)
    r = session_b.get(f"{BASE_URL}/api/goals/{goal_id}")
    print(f"User B gets User A's Goal: Status = {r.status_code}")
    assert r.status_code == 403

    # 6. Reports
    print("\n--- 6. Reports & Analytics ---")
    r = session_a.get(f"{BASE_URL}/api/reports/monthly/2026/5")
    print(f"Monthly Report A: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 200
    report = r.json()
    assert report["totalIncome"]["Salary"] == 5500.00
    assert report["totalExpenses"]["Rent"] == 1200.00
    assert report["totalExpenses"]["Food"] == 150.50
    assert report["netSavings"] == 4149.50

    r = session_a.get(f"{BASE_URL}/api/reports/yearly/2026")
    print(f"Yearly Report A: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 200
    assert r.json()["netSavings"] == 4149.50

    # 7. Category Dependency Check
    print("\n--- 7. Category Delete & Reference Validation ---")
    r = session_a.post(f"{BASE_URL}/api/categories", json={"name": "Taxes", "type": "EXPENSE"})
    assert r.status_code == 201
    
    r = session_a.post(f"{BASE_URL}/api/transactions", json={"amount": 200.0, "date": "2026-05-10", "category": "Taxes"})
    assert r.status_code == 201
    
    r = session_a.delete(f"{BASE_URL}/api/categories/Taxes")
    print(f"Delete Referenced Custom Category: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 409

    # 8. Logout
    print("\n--- 8. Logout ---")
    r = session_a.post(f"{BASE_URL}/api/auth/logout")
    print(f"Logout User A: Status = {r.status_code}, Body = {r.json()}")
    assert r.status_code == 200
    assert "Logout successful" in r.json()["message"]

    r = session_a.get(f"{BASE_URL}/api/transactions")
    print(f"Access after logout: Status = {r.status_code}")
    assert r.status_code == 401

    print("\n====================================================")
    print("      ALL E2E VERIFICATION TESTS PASSED SUCCESSFULLY!      ")
    print("====================================================")

if __name__ == "__main__":
    try:
        run_tests()
    except Exception as e:
        print(f"\nTEST FAILURE: {e}", file=sys.stderr)
        sys.exit(1)
