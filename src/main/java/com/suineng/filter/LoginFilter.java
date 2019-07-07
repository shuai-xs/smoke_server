package com.suineng.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;

import com.suineng.cache.UserSessionCache;

@WebFilter(urlPatterns = "/*", filterName = "loginFilter")
public class LoginFilter implements Filter {

    @Value("${login.uri:/login}")
    private String LOGIN_PAGE;

    @Value("${login.index:/login}")
    private String LOGIN_INDEX;

    @Value("${session.auth:false}")
    private boolean isAuth;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private boolean auth(HttpServletRequest request, HttpServletResponse response)
    {
        if(isAuth)
        {
            HttpSession session = request.getSession();
            String userName = UserSessionCache.getInstance().getUserName(session.getId());
            if(null == userName)
            {
                return false;
            }
            return true;
        }
        return true;
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
        String uri = httpServletRequest.getRequestURI();
        if(!uri.startsWith(LOGIN_INDEX) && !"/reset".equals(uri))
        {
            //未登陆则需要用户登录
            if(!auth(httpServletRequest, httpServletResponse))
            {
                httpServletResponse.sendRedirect(LOGIN_PAGE);
            }
            else
            {
                filterChain.doFilter(servletRequest,servletResponse);
            }
        }
        else
        {
            filterChain.doFilter(servletRequest,servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
