package br.edu.infnet.indiestage.espacos.config;
import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component
public class CorrelationFilter extends OncePerRequestFilter {
    private static final Logger log=LoggerFactory.getLogger(CorrelationFilter.class);
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)
            throws ServletException,IOException {
        String id=req.getHeader("X-Correlation-Id");
        if(id==null || !id.matches("[a-zA-Z0-9._-]{1,80}")) id=UUID.randomUUID().toString();
        MDC.put("correlationId",id); res.setHeader("X-Correlation-Id",id);long inicio=System.nanoTime();
        try {chain.doFilter(req,res);} finally {
            log.info("metodo={} rota={} status={} duracaoMs={}",req.getMethod(),req.getRequestURI(),
                    res.getStatus(),(System.nanoTime()-inicio)/1000000);
            MDC.remove("correlationId");
        }
    }
}