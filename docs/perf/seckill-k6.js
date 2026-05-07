/**
 * k6 秒杀接口压测示例（需 campus.seckill.enabled=true 且 Redis/Rabbit/DB 可用）
 *
 * 运行示例：
 *   k6 run --vus 50 --duration 60s -e BASE_URL=http://localhost:8080 -e JWT=your_jwt_here docs/perf/seckill-k6.js
 *
 * 目标：在实验室环境配合 ticket 预热与足够 burstCapacity，可观察 800+ RPS 量级（实际取决于单机与依赖）。
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const JWT = __ENV.JWT || '';
const CAMPUS_ID = __ENV.CAMPUS_ID || '1';
const TICKET_TYPE_ID = __ENV.TICKET_TYPE_ID || '1';

export default function () {
  const url = `${BASE}/api/v3/seckill/ticket-orders`;
  const payload = JSON.stringify({
    campusId: Number(CAMPUS_ID),
    ticketTypeId: Number(TICKET_TYPE_ID),
  });
  const params = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: JWT ? `Bearer ${JWT}` : '',
    },
  };
  const res = http.post(url, payload, params);
  check(res, { '2xx or 429': (r) => (r.status >= 200 && r.status < 300) || r.status === 429 });
  sleep(0.05);
}
