import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 50, // 50 concurrent virtual users
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<250'], // 95% of requests must complete in <250ms
    http_req_failed: ['rate<0.01'],    // Unexpected failures must be under 1%
  },
};

export default function () {
  const url = 'http://localhost:8080/api/v1/procurement/requisitions/PR-2026-11-0001/approve';
  const payload = JSON.stringify({
    approverId: 'emp-mgr-001',
    note: 'Approved via k6 high-concurrency benchmark',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer DUMMY_STRESS_TOKEN',
      'X-Idempotency-Key': `k6-stress-${__VU}-${__ITER}`,
    },
  };

  const res = http.post(url, payload, params);

  // Optimistic locking defense ensures exactly one transaction commits, subsequent parallel attempts return 409 Conflict safely
  check(res, {
    'Status is 200 or 409': (r) => r.status === 200 || r.status === 409,
  });

  sleep(0.05);
}

