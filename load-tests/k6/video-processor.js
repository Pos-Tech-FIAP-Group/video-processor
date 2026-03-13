import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.K6_BASE_URL || 'http://localhost:8080';
const VIDEO_FILE_PATH = __ENV.K6_VIDEO_FILE || '';
const VIDEO_FILE_NAME = __ENV.K6_VIDEO_FILE_NAME || 'sample.mp4';
const VIDEO_CONTENT_TYPE = __ENV.K6_VIDEO_CONTENT_TYPE || 'video/mp4';
const FRAME_INTERVAL_SECONDS = __ENV.K6_FRAME_INTERVAL_SECONDS || '1';
const REPORT_BASENAME = __ENV.K6_REPORT_BASENAME || 'load-tests/reports/latest';

const UPLOAD_VUS = Number(__ENV.K6_UPLOAD_VUS || 1);
const UPLOAD_DURATION = __ENV.K6_UPLOAD_DURATION || '30s';
const UPLOAD_SLEEP_SECONDS = Number(__ENV.K6_UPLOAD_SLEEP_SECONDS || 1);

const uploadAttempts = new Counter('upload_attempts');
const uploadSucceeded = new Counter('upload_succeeded');
const uploadFailed = new Counter('upload_failed');
const uploadTimeouts = new Counter('upload_timeouts');

const uploadFile = VIDEO_FILE_PATH
    ? http.file(open(VIDEO_FILE_PATH, 'b'), VIDEO_FILE_NAME, VIDEO_CONTENT_TYPE)
    : null;

export const options = {
    scenarios: {
        upload_videos: {
            executor: 'constant-vus',
            exec: 'uploadVideoScenario',
            vus: UPLOAD_VUS,
            duration: UPLOAD_DURATION,
            gracefulStop: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<3000'],
        checks: ['rate>0.95'],
    },
};

function authHeaders(token, userUuid) {
    return {
        headers: {
            Authorization: `Bearer ${token}`,
            'X-User-Id': userUuid,
        },
    };
}

function uniqueSuffix() {
    return `${Date.now()}-${Math.floor(Math.random() * 1_000_000)}`;
}

function safeJson(response) {
    try {
        return response.json();
    } catch (_) {
        return null;
    }
}

function registerAndLogin() {
    const suffix = uniqueSuffix();
    const username = `k6_user_${suffix}`;
    const password = 'senha123';
    const email = `k6_${suffix}@example.com`;

    const registerResponse = http.post(
        `${BASE_URL}/api/auth/register`,
        JSON.stringify({ username, email, password }),
        { headers: { 'Content-Type': 'application/json' } },
    );

    check(registerResponse, {
        'register returned 201': (response) => response.status === 201,
    });

    if (registerResponse.status !== 201) {
        fail(`Falha no register: status=${registerResponse.status} body=${registerResponse.body}`);
    }

    const registeredUser = safeJson(registerResponse);

    const loginResponse = http.post(
        `${BASE_URL}/api/auth/login`,
        JSON.stringify({ username, password }),
        { headers: { 'Content-Type': 'application/json' } },
    );

    check(loginResponse, {
        'login returned 200': (response) => response.status === 200,
        'login returned token': (response) => Boolean(response.json('token')),
    });

    if (loginResponse.status !== 200) {
        fail(`Falha no login: status=${loginResponse.status} body=${loginResponse.body}`);
    }

    const loginBody = loginResponse.json();
    const userUuid = registeredUser?.userUuid || loginBody?.userUuid;

    if (!userUuid) {
        fail(`Falha ao obter userUuid para o teste de carga: register=${registerResponse.body} login=${loginResponse.body}`);
    }

    return {
        token: loginBody.token,
        userUuid,
    };
}

export function setup() {
    if (!uploadFile) {
        fail('Defina K6_VIDEO_FILE com o caminho do video que sera enviado no POST /api/videos.');
    }

    return registerAndLogin();
}

export function uploadVideoScenario(auth) {
    const payload = {
        file: uploadFile,
    };

    uploadAttempts.add(1);

    const response = http.post(
        `${BASE_URL}/api/videos?frameIntervalSeconds=${FRAME_INTERVAL_SECONDS}`,
        payload,
        {
            ...authHeaders(auth.token, auth.userUuid),
            tags: { endpoint: 'post_videos' },
        },
    );

    if (response.error) {
        uploadFailed.add(1);

        if (String(response.error).toLowerCase().includes('timeout')) {
            uploadTimeouts.add(1);
        }
    }

    const uploadAccepted = check(response, {
        'upload returned 202': (res) => res.status === 202,
        'upload returned id': (res) => res.status === 202 && Boolean(res.json('id')),
        'upload returned status': (res) => res.status === 202 && Boolean(res.json('status')),
    });

    if (uploadAccepted) {
        uploadSucceeded.add(1);
    } else if (!response.error) {
        uploadFailed.add(1);
    }

    sleep(UPLOAD_SLEEP_SECONDS);
}

function metricValue(metric, field) {
    if (!metric || !metric.values || metric.values[field] === undefined) {
        return 'n/a';
    }

    const value = metric.values[field];
    return typeof value === 'number' ? value.toFixed(2) : String(value);
}

function rateAsPercent(metric) {
    if (!metric || !metric.values || metric.values.rate === undefined) {
        return 'n/a';
    }

    return (metric.values.rate * 100).toFixed(2);
}

function counterValue(metric) {
    if (!metric || !metric.values || metric.values.count === undefined) {
        return 'n/a';
    }

    return String(metric.values.count);
}

function renderHtmlReport(data) {
    const durationMetric = data.metrics.http_req_duration;
    const failedMetric = data.metrics.http_req_failed;
    const checksMetric = data.metrics.checks;
    const attemptsMetric = data.metrics.upload_attempts;
    const succeededMetric = data.metrics.upload_succeeded;
    const failedUploadsMetric = data.metrics.upload_failed;
    const timeoutMetric = data.metrics.upload_timeouts;

    return `<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8">
  <title>K6 Report</title>
  <style>
    body { font-family: Segoe UI, Arial, sans-serif; margin: 32px; color: #1f2937; background: #f8fafc; }
    h1 { margin-bottom: 8px; }
    .meta { margin-bottom: 24px; color: #475569; }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .card { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06); }
    .label { font-size: 12px; text-transform: uppercase; color: #64748b; margin-bottom: 8px; }
    .value { font-size: 28px; font-weight: 700; }
    table { width: 100%; border-collapse: collapse; background: white; border-radius: 12px; overflow: hidden; }
    th, td { padding: 12px 14px; border-bottom: 1px solid #e2e8f0; text-align: left; }
    th { background: #e2e8f0; }
  </style>
</head>
<body>
  <h1>Relatorio de carga k6</h1>
  <div class="meta">Gerado em ${new Date().toISOString()} | Base URL: ${BASE_URL}</div>
  <div class="grid">
    <div class="card"><div class="label">Uploads Tentados</div><div class="value">${counterValue(attemptsMetric)}</div></div>
    <div class="card"><div class="label">Uploads OK</div><div class="value">${counterValue(succeededMetric)}</div></div>
    <div class="card"><div class="label">Uploads Falharam</div><div class="value">${counterValue(failedUploadsMetric)}</div></div>
    <div class="card"><div class="label">Timeouts</div><div class="value">${counterValue(timeoutMetric)}</div></div>
    <div class="card"><div class="label">Checks</div><div class="value">${rateAsPercent(checksMetric)}%</div></div>
    <div class="card"><div class="label">Falhas HTTP</div><div class="value">${rateAsPercent(failedMetric)}%</div></div>
    <div class="card"><div class="label">P95</div><div class="value">${metricValue(durationMetric, 'p(95)')} ms</div></div>
    <div class="card"><div class="label">Req/s</div><div class="value">${metricValue(data.metrics.http_reqs, 'rate')}</div></div>
  </div>
  <table>
    <thead>
      <tr>
        <th>Metrica</th>
        <th>avg</th>
        <th>min</th>
        <th>med</th>
        <th>p(90)</th>
        <th>p(95)</th>
        <th>max</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>http_req_duration</td>
        <td>${metricValue(durationMetric, 'avg')}</td>
        <td>${metricValue(durationMetric, 'min')}</td>
        <td>${metricValue(durationMetric, 'med')}</td>
        <td>${metricValue(durationMetric, 'p(90)')}</td>
        <td>${metricValue(durationMetric, 'p(95)')}</td>
        <td>${metricValue(durationMetric, 'max')}</td>
      </tr>
      <tr>
        <td>http_req_waiting</td>
        <td>${metricValue(data.metrics.http_req_waiting, 'avg')}</td>
        <td>${metricValue(data.metrics.http_req_waiting, 'min')}</td>
        <td>${metricValue(data.metrics.http_req_waiting, 'med')}</td>
        <td>${metricValue(data.metrics.http_req_waiting, 'p(90)')}</td>
        <td>${metricValue(data.metrics.http_req_waiting, 'p(95)')}</td>
        <td>${metricValue(data.metrics.http_req_waiting, 'max')}</td>
      </tr>
    </tbody>
  </table>
</body>
</html>`;
}

export function handleSummary(data) {
    const consoleSummary = [
        '',
        'K6 summary',
        `Base URL: ${BASE_URL}`,
        `upload_attempts: ${counterValue(data.metrics.upload_attempts)}`,
        `upload_succeeded: ${counterValue(data.metrics.upload_succeeded)}`,
        `upload_failed: ${counterValue(data.metrics.upload_failed)}`,
        `upload_timeouts: ${counterValue(data.metrics.upload_timeouts)}`,
        `http_req_failed.rate: ${rateAsPercent(data.metrics.http_req_failed)}%`,
        `http_req_duration.avg: ${metricValue(data.metrics.http_req_duration, 'avg')} ms`,
        `http_req_duration.p(95): ${metricValue(data.metrics.http_req_duration, 'p(95)')} ms`,
        `checks.rate: ${rateAsPercent(data.metrics.checks)}%`,
        '',
    ].join('\n');

    return {
        stdout: consoleSummary,
        [`${REPORT_BASENAME}-summary.json`]: JSON.stringify(data, null, 2),
        [`${REPORT_BASENAME}.html`]: renderHtmlReport(data),
    };
}
