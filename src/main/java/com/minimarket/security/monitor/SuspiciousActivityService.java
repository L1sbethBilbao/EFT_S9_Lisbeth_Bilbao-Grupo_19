package com.minimarket.security.monitor;

import com.minimarket.security.config.SuspiciousActivityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SuspiciousActivityService {

    private static final Logger log = LoggerFactory.getLogger(SuspiciousActivityService.class);

    private final SuspiciousActivityProperties properties;
    private final ConcurrentHashMap<String, List<Long>> failedLoginTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Long>> requestTimestampsByIp = new ConcurrentHashMap<>();

    public SuspiciousActivityService(SuspiciousActivityProperties properties) {
        this.properties = properties;
    }

    public String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return xf != null && !xf.isBlank() ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }

    private void pruneOld(List<Long> list) {
        long cutoff = Instant.now().toEpochMilli() - properties.getWindowMs();
        list.removeIf(t -> t < cutoff);
    }

    public void recordFailedLogin(HttpServletRequest req, String username) {
        String ip = clientIp(req);
        String key = "FAILED_LOGIN:" + (username == null || username.isBlank() ? ip : username + "@" + ip);
        List<Long> list = failedLoginTimestamps.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (list) {
            list.add(Instant.now().toEpochMilli());
            pruneOld(list);
            int count = list.size();
            log.warn("SuspiciousActivity: failed login (user={}, ip={}, count={})", username, ip, count);
            if (count >= properties.getFailedLoginThreshold()) {
                log.warn("SuspiciousActivity: threshold reached for failed logins (user/ip={}): {}", key, count);
            }
        }
    }

    public void recordInvalidJwt(HttpServletRequest req, Exception ex) {
        String ip = clientIp(req);
        log.warn("SuspiciousActivity: invalid JWT from ip={} path={} reason={}",
                ip, req.getRequestURI(), ex == null ? "invalid/expired" : ex.getMessage());
    }

    public void recordRequest(HttpServletRequest req) {
        String ip = clientIp(req);
        List<Long> list = requestTimestampsByIp.computeIfAbsent(ip, k -> new ArrayList<>());
        synchronized (list) {
            list.add(Instant.now().toEpochMilli());
            pruneOld(list);
            int count = list.size();
            if (count % 50 == 0) {
                log.info("SuspiciousActivity: request rate ip={} count_last_{}m={}",
                        ip, properties.getWindowMinutes(), count);
            }
            if (count >= properties.getRequestThreshold()) {
                log.warn("SuspiciousActivity: high request rate detected ip={} count_last_{}m={}",
                        ip, properties.getWindowMinutes(), count);
            }
        }
    }

    public void recordUnauthorizedAccess(HttpServletRequest req, String resource) {
        String ip = clientIp(req);
        log.warn("SuspiciousActivity: unauthorized access attempt ip={} path={} resource={}",
                ip, req.getRequestURI(), resource);
    }

    public void recordCrudOperation(HttpServletRequest req, String operation, String resource) {
        String ip = clientIp(req);
        log.info("SuspiciousActivity: CRUD op={} resource={} by ip={} path={}",
                operation, resource, ip, req.getRequestURI());
    }
}
