<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<c:if test="${empty user}">
    <c:redirect url="/"/>
</c:if>

<section class="card">
    <h1>Thanks for joining our email list</h1>
    <p>Here is the information that you entered:</p>
    <table class="info">
        <tr><th>First Name</th><td><c:out value="${user.firstName}"/></td></tr>
        <tr><th>Last Name</th><td><c:out value="${user.lastName}"/></td></tr>
        <tr><th>Email</th><td><c:out value="${user.email}"/></td></tr>
        <tr><th>Email format</th><td><c:out value="${format eq 'html' ? 'text/html' : 'text/plain'}"/></td></tr>
        <tr><th>MAIL_MODE</th><td><code><c:out value="${mailMode}"/></code></td></tr>
    </table>

    <c:choose>
        <c:when test="${not empty errorMessage}">
            <p class="alert error"><c:out value="${errorMessage}"/></p>
        </c:when>
        <c:when test="${emailSent}">
            <p class="alert ok">A welcome email has been sent to
                <b><c:out value="${user.email}"/></b>. Check your inbox (and spam folder).</p>
        </c:when>
        <c:otherwise>
            <p class="alert info">Demo mode (<code>MAIL_MODE=log</code>): the email was written to
                the server log instead of being sent. Set <code>MAIL_MODE</code> to
                <code>gmail</code>, <code>resend</code> or <code>local</code> to really send it.</p>
            <h3>Email preview</h3>
            <c:choose>
                <c:when test="${emailPreviewHtml}">
                    <iframe class="preview" sandbox="" title="Email preview"
                            srcdoc="<c:out value='${emailPreview}'/>"></iframe>
                </c:when>
                <c:otherwise>
                    <pre class="preview"><c:out value="${emailPreview}"/></pre>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>

    <p>To enter another email address, click on the Back button in your browser or the Return button shown below.</p>
    <form action="<c:url value='/emailList'/>" method="get">
        <button type="submit">Return</button>
    </form>
</section>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
