import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || 'emreyuksell78@gmail.com';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || '123';

export default function () {
  const loginRes = http.post(
    `${BASE_URL}/api/users/login`,
    JSON.stringify({ email: LOGIN_EMAIL, password: LOGIN_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(loginRes, { 'login status 200': (r) => r.status === 200 });

  let userId = 'demo-user';
  try {
    const body = JSON.parse(loginRes.body);
    if (body.id) userId = body.id;
  } catch (_) {}

  const headers = { 'Content-Type': 'application/json' };

  const products = http.get(`${BASE_URL}/api/products`);
  check(products, { 'products 200': (r) => r.status === 200 });

  const transactions = http.get(`${BASE_URL}/api/transactions`);
  check(transactions, { 'transactions 200': (r) => r.status === 200 });

  const budgets = http.get(`${BASE_URL}/api/budgets`);
  check(budgets, { 'budgets 200': (r) => r.status === 200 });

  const accounts = http.get(`${BASE_URL}/api/accounts`);
  check(accounts, { 'accounts 200': (r) => r.status === 200 });

  const goals = http.get(`${BASE_URL}/api/goals`);
  check(goals, { 'goals 200': (r) => r.status === 200 });

  const createTx = http.post(
    `${BASE_URL}/api/transactions`,
    JSON.stringify({
      userId,
      amount: 10.5,
      type: 'GIDER',
      description: 'k6 load test',
    }),
    { headers }
  );
  check(createTx, { 'create transaction 201': (r) => r.status === 201 });

  sleep(1);
}

// Stress senaryosu (isteğe bağlı):
// k6 run --env BASE_URL=http://localhost:8080 k6/load-test.js --stage 2m:100
