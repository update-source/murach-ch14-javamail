<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<section class="card">
    <h1>Oops!</h1>
    <p>Status: <c:out value="${requestScope['javax.servlet.error.status_code']}"/></p>
    <p>Sorry, the page you requested is not available or an error occurred.</p>
    <p><a href="<c:url value='/'/>">Back to the home page</a></p>
</section>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
