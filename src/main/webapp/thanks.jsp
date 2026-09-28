<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<c:if test="${empty user}">
    <c:redirect url="/"/>
</c:if>

<h1>Thanks for joining our email list</h1>

<p>Here is the information that you entered:</p>

<label>Email:</label>
<span><c:out value="${user.email}"/></span><br>
<label>First Name:</label>
<span><c:out value="${user.firstName}"/></span><br>
<label>Last Name:</label>
<span><c:out value="${user.lastName}"/></span><br>
<label>Email format:</label>
<span><c:out value="${format eq 'html' ? 'text/html' : 'text/plain'}"/></span><br>

<c:choose>
    <c:when test="${not empty errorMessage}">
        <p class="error"><i><c:out value="${errorMessage}"/></i></p>
    </c:when>
    <c:when test="${emailSent}">
        <p class="success">A welcome email has been sent to <c:out value="${user.email}"/>.</p>
    </c:when>
    <c:otherwise>
        <p><i>MAIL_MODE=log: the email was written to the server log instead of being sent.
           Here is the email:</i></p>
        <c:choose>
            <c:when test="${emailPreviewHtml}">
                <iframe sandbox="" title="Email preview" width="600" height="250"
                        srcdoc="<c:out value='${emailPreview}'/>"></iframe>
            </c:when>
            <c:otherwise>
                <pre><c:out value="${emailPreview}"/></pre>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>

<p>To enter another email address, click on the Back
   button in your browser or the Return button shown
   below.</p>

<form action="<c:url value='/emailList'/>" method="get">
    <input type="submit" value="Return">
</form>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
