#!/usr/bin/env python3
"""
Guardix Backend Integration Test
Tests the backend API endpoints to ensure they're working correctly
"""

import requests
import json
from datetime import datetime

# Configuration
BASE_URL = "http://localhost:8000"
COLORS = {
    'GREEN': '\033[92m',
    'RED': '\033[91m',
    'YELLOW': '\033[93m',
    'BLUE': '\033[94m',
    'END': '\033[0m'
}

def print_colored(text, color='BLUE'):
    """Print colored text to console"""
    print(f"{COLORS.get(color, '')}{text}{COLORS['END']}")

def test_endpoint(method, endpoint, data=None, headers=None, expected_status=200):
    """Test a single endpoint"""
    url = f"{BASE_URL}{endpoint}"
    
    try:
        if method == 'GET':
            response = requests.get(url, headers=headers, timeout=5)
        elif method == 'POST':
            response = requests.post(url, json=data, headers=headers, timeout=5)
        else:
            return False, f"Unsupported method: {method}"
        
        if response.status_code == expected_status:
            return True, response.json()
        else:
            return False, f"Expected {expected_status}, got {response.status_code}"
    
    except requests.exceptions.ConnectionError:
        return False, "Connection refused - Is the backend running?"
    except requests.exceptions.Timeout:
        return False, "Request timeout"
    except Exception as e:
        return False, str(e)

def main():
    print_colored("\n" + "="*60, 'BLUE')
    print_colored("  Guardix Backend Integration Test", 'BLUE')
    print_colored("="*60 + "\n", 'BLUE')
    
    print_colored(f"Testing backend at: {BASE_URL}", 'YELLOW')
    print_colored(f"Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n", 'YELLOW')
    
    tests_passed = 0
    tests_failed = 0
    
    # Test 1: Health Check
    print_colored("1. Testing Health Check (GET /)", 'BLUE')
    success, result = test_endpoint('GET', '/')
    if success:
        print_colored(f"   ✅ PASSED - {result.get('message', 'OK')}", 'GREEN')
        tests_passed += 1
    else:
        print_colored(f"   ❌ FAILED - {result}", 'RED')
        tests_failed += 1
        print_colored("\n⚠️  Backend is not running! Please start it first.", 'RED')
        print_colored("   Run: python -m app.main", 'YELLOW')
        return
    
    # Get auth token first
    print_colored("\n2. Testing Authentication (POST /auth/login)", 'BLUE')
    auth_data = {
        "device_id": "test-device-001",
        "device_name": "Integration Test Device"
    }
    success, result = test_endpoint('POST', '/auth/login', data=auth_data)
    
    if success:
        token = result.get('access_token')
        print_colored(f"   ✅ PASSED - Token received", 'GREEN')
        tests_passed += 1
        headers = {"Authorization": f"Bearer {token}"}
    else:
        print_colored(f"   ❌ FAILED - {result}", 'RED')
        tests_failed += 1
        headers = {}
    
    # Test 3: Performance - Memory Status
    print_colored("\n3. Testing Performance API (GET /performance/memory)", 'BLUE')
    success, result = test_endpoint('GET', '/performance/memory', headers=headers)
    if success:
        print_colored(f"   ✅ PASSED", 'GREEN')
        tests_passed += 1
    else:
        print_colored(f"   ❌ FAILED - {result}", 'RED')
        tests_failed += 1
    
    # Test 4: Network Usage
    print_colored("\n4. Testing Network API (GET /network-tools/usage)", 'BLUE')
    success, result = test_endpoint('GET', '/network-tools/usage', headers=headers)
    if success:
        print_colored(f"   ✅ PASSED", 'GREEN')
        tests_passed += 1
    else:
        print_colored(f"   ❌ FAILED - {result}", 'RED')
        tests_failed += 1
    
    # Test 5: Security Overview
    print_colored("\n5. Testing Security API (GET /security-tools/overview)", 'BLUE')
    success, result = test_endpoint('GET', '/security-tools/overview', headers=headers)
    if success:
        print_colored(f"   ✅ PASSED", 'GREEN')
        tests_passed += 1
    else:
        print_colored(f"   ❌ FAILED - {result}", 'RED')
        tests_failed += 1
    
    # Test 6: Storage Overview
    print_colored("\n6. Testing Storage API (GET /storage/storage-overview)", 'BLUE')
    success, result = test_endpoint('GET', '/storage/storage-overview', headers=headers)
    if success:
        print_colored(f"   ✅ PASSED", 'GREEN')
        tests_passed += 1
    else:
        print_colored(f"   ❌ FAILED - {result}", 'RED')
        tests_failed += 1
    
    # Test 7: Anomaly Detection
    print_colored("\n7. Testing Anomaly Detection (POST /anomaly/behavior)", 'BLUE')
    anomaly_data = {
        "features": [0.5, 0.3, 0.8, 0.2, 0.6, 0.4, 0.7, 0.1, 0.9, 0.3]
    }
    success, result = test_endpoint('POST', '/anomaly/behavior', data=anomaly_data, headers=headers)
    if success:
        print_colored(f"   ✅ PASSED - Anomaly score: {result.get('anomaly_score', 'N/A')}", 'GREEN')
        tests_passed += 1
    else:
        print_colored(f"   ❌ FAILED - {result}", 'RED')
        tests_failed += 1
    
    # Test 8: Models Info
    print_colored("\n8. Testing Models API (GET /models/)", 'BLUE')
    success, result = test_endpoint('GET', '/models/', headers=headers)
    if success:
        print_colored(f"   ✅ PASSED", 'GREEN')
        tests_passed += 1
    else:
        print_colored(f"   ❌ FAILED - {result}", 'RED')
        tests_failed += 1
    
    # Summary
    total_tests = tests_passed + tests_failed
    pass_rate = (tests_passed / total_tests * 100) if total_tests > 0 else 0
    
    print_colored("\n" + "="*60, 'BLUE')
    print_colored("  Test Summary", 'BLUE')
    print_colored("="*60, 'BLUE')
    print_colored(f"  Total Tests: {total_tests}", 'YELLOW')
    print_colored(f"  Passed: {tests_passed}", 'GREEN')
    print_colored(f"  Failed: {tests_failed}", 'RED')
    print_colored(f"  Pass Rate: {pass_rate:.1f}%", 'YELLOW')
    print_colored("="*60 + "\n", 'BLUE')
    
    if tests_failed == 0:
        print_colored("🎉 All tests passed! Backend is ready for mobile app integration.", 'GREEN')
    else:
        print_colored("⚠️  Some tests failed. Please check the backend logs.", 'YELLOW')
    
    return tests_failed == 0

if __name__ == "__main__":
    import sys
    success = main()
    sys.exit(0 if success else 1)
