<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/includes/header.jsp" %>

<h1>Chapter 14 &ndash; Câu hỏi &amp; trả lời</h1>
<p>Trả lời đầy đủ các mục tiêu (Objectives) của chương và các câu hỏi ôn tập về JavaMail.</p>

<h2>Objectives</h2>

<div>
<h3>Applied 1. Develop servlets that send email messages to the users of the application.</h3>
<p>Đã hiện thực trong project này:</p>
<ul>
    <li><code>EmailListServlet</code> (<code>/emailList</code>): nhận form, kiểm tra dữ liệu, lưu <code>User</code>
        vào <code>UserDB</code>, tạo email chào mừng và gửi bằng JavaMail; nếu có <code>MessagingException</code>
        thì ghi log và hiển thị lỗi trên <code>thanks.jsp</code>.</li>
    <li><code>MailUtilLocal</code>: gửi qua SMTP server cục bộ (<code>localhost:25</code>, không xác thực, <code>Transport.send</code>).</li>
    <li><code>MailUtilGmail</code>: gửi qua SMTP server từ xa (<code>smtps</code>, <code>smtp.gmail.com:465</code>, có xác thực,
        <code>getTransport/connect/sendMessage/close</code>).</li>
    <li><code>AdminServlet</code> (<code>/admin</code>): gửi một thông báo tới nhiều người nhận cùng lúc
        (<code>setRecipients</code> với mảng <code>Address[]</code>, đặt ở BCC).</li>
</ul>
</div>

<div>
<h3>Knowledge 1. In terms of the SMTP, POP, and MIME protocols, describe how an email message is sent from one client to another.</h3>
<pre>Mail client (gửi) --SMTP--&gt; Mail server gửi --SMTP--&gt; Mail server nhận --POP/IMAP--&gt; Mail client (nhận)</pre>
<ol>
    <li>Phần mềm mail client của người gửi soạn thư. Nội dung thư được đóng gói theo chuẩn <b>MIME</b>:
        MIME cho biết kiểu nội dung (<code>text/plain</code>, <code>text/html</code>, ảnh, file đính kèm...) và bảng mã (UTF-8).</li>
    <li>Client gửi thư đến mail server của người gửi bằng giao thức <b>SMTP</b> (Simple Mail Transfer Protocol).</li>
    <li>Mail server gửi tiếp chuyển thư sang mail server của người nhận, cũng bằng <b>SMTP</b>
        (SMTP dùng để chuyển thư từ server này sang server khác).</li>
    <li>Thư được lưu trong hộp thư trên mail server nhận. Mail client của người nhận dùng <b>POP</b> (Post Office Protocol)
        để tải thư từ server về client (hoặc <b>IMAP</b> để đọc thư ngay trên server).</li>
    <li>Client nhận đọc header MIME để biết cách hiển thị nội dung (văn bản thuần, HTML hay tệp đính kèm).</li>
</ol>
<p>Trong ứng dụng này, servlet đóng vai trò &ldquo;sending client&rdquo;: JavaMail nói chuyện với SMTP server (localhost hoặc Gmail).</p>
</div>

<h2>Câu hỏi ôn tập</h2>

<div>
<h3>1. SMTP, POP, IMAP và MIME là gì?</h3>
<table>
    <tr><th>Giao thức</th><th>Mô tả</th></tr>
    <tr><td>SMTP</td><td>Simple Mail Transfer Protocol &ndash; gửi thư từ client lên server và từ mail server này sang mail server khác. Cổng 25 (không mã hoá), 465 (SMTPS/SSL), 587 (STARTTLS).</td></tr>
    <tr><td>POP (POP3)</td><td>Post Office Protocol &ndash; chuyển thư từ mail server về mail client (thường tải về rồi xoá trên server).</td></tr>
    <tr><td>IMAP</td><td>Internet Message Access Protocol &ndash; cho phép client/web browser đọc thư đang lưu trên mail server, đồng bộ nhiều thiết bị.</td></tr>
    <tr><td>MIME</td><td>Multipurpose Internet Mail Extension &ndash; quy định kiểu nội dung có thể gửi trong thư hoặc tệp đính kèm (text/plain, text/html, image/png...).</td></tr>
</table>
</div>

<div>
<h3>2. Cần những file JAR nào để dùng JavaMail API? Cài đặt thế nào?</h3>
<ul>
    <li><code>javax.mail.jar</code>: chứa các lớp của JavaMail API.</li>
    <li><code>activation.jar</code>: JavaBeans Activation Framework (JAF) &ndash; JavaMail cần JAF để chạy.</li>
</ul>
<p>Cách cài: tải <code>javax.mail.jar</code>, chép vào thư mục <code>WEB-INF\lib</code> của ứng dụng và thêm vào classpath.
   Project này dùng Maven nên chỉ cần khai báo dependency <code>com.sun.mail:javax.mail:1.6.2</code>; Maven tự đóng gói JAR vào <code>WEB-INF/lib</code>.</p>
<p><b>Lưu ý hiện đại:</b> slide nói JAF có sẵn từ Java SE 6, nhưng JAF đã bị <b>loại bỏ khỏi JDK từ Java 11</b>, vì vậy trên Java 11+ phải thêm
   <code>javax.activation</code> (project này đã thêm trong <code>pom.xml</code>).</p>
</div>

<div>
<h3>3. Ba package dùng để gửi email là gì?</h3>
<ul>
    <li><code>java.util</code>: lớp <code>Properties</code> để đặt thuộc tính cho mail session.</li>
    <li><code>javax.mail</code>: <code>Session</code>, <code>Message</code>, <code>Address</code>, <code>Transport</code>, <code>MessagingException</code>.</li>
    <li><code>javax.mail.internet</code>: <code>MimeMessage</code>, <code>InternetAddress</code> để gửi thư qua Internet.</li>
</ul>
</div>

<div>
<h3>4. Bốn bước gửi một email bằng JavaMail?</h3>
<ol>
    <li><b>Lấy mail session</b>: tạo <code>Properties</code> (<code>mail.smtp.host</code>...) rồi gọi <code>Session.getDefaultInstance(props)</code> / <code>Session.getInstance(props)</code>.</li>
    <li><b>Tạo message</b>: <code>new MimeMessage(session)</code>, <code>setSubject</code>, <code>setText</code> hoặc <code>setContent</code>.</li>
    <li><b>Đặt địa chỉ</b>: <code>setFrom(new InternetAddress(from))</code>, <code>setRecipient(Message.RecipientType.TO, ...)</code>.</li>
    <li><b>Gửi</b>: <code>Transport.send(message)</code> (hoặc dùng đối tượng <code>Transport</code> khi cần xác thực).</li>
</ol>
</div>

<div>
<h3>5. Các thuộc tính của Session và cách lấy session cho SMTP server cục bộ / từ xa?</h3>
<table>
    <tr><th>Property</th><th>Ý nghĩa</th></tr>
    <tr><td><code>mail.transport.protocol</code></td><td>Giao thức gửi: thường là <code>smtp</code> hoặc <code>smtps</code>.</td></tr>
    <tr><td><code>mail.smtp.host</code></td><td>Máy chủ SMTP.</td></tr>
    <tr><td><code>mail.smtp.port</code></td><td>Cổng của SMTP server.</td></tr>
    <tr><td><code>mail.smtp.auth</code></td><td>Có cần đăng nhập SMTP server hay không.</td></tr>
    <tr><td><code>mail.smtp.quitwait</code></td><td>Đặt <code>false</code> để tránh <code>SSLException</code> khi kết nối Gmail.</td></tr>
</table>
<p><b>Local</b>: <code>mail.transport.protocol=smtp</code>, <code>mail.smtp.host=localhost</code>, <code>mail.smtp.port=25</code>.<br>
   <b>Remote (Gmail)</b>: <code>mail.transport.protocol=smtps</code>, <code>mail.smtps.host=smtp.gmail.com</code>, <code>mail.smtps.port=465</code>,
   <code>mail.smtps.auth=true</code>, <code>mail.smtps.quitwait=false</code>. Khi protocol là <code>smtps</code> thì tên thuộc tính cũng đổi tiền tố thành <code>mail.smtps.*</code>.</p>
<p><code>session.setDebug(true)</code> in thông tin hội thoại SMTP ra log để gỡ lỗi. Nếu ứng dụng chạy cùng máy với SMTP server thì dùng <code>localhost</code>.</p>
</div>

<div>
<h3>6. Tại sao đổi thuộc tính Session phải khởi động lại Tomcat?</h3>
<p><code>Session.getDefaultInstance</code> tạo session mặc định <b>một lần</b> rồi dùng chung cho cả JVM; các lần gọi sau trả về session cũ và bỏ qua
   <code>Properties</code> mới. Vì vậy phải restart Tomcat thì thay đổi mới có hiệu lực. Project này dùng <code>Session.getInstance(props)</code>
   để mỗi lần gửi tạo session mới theo cấu hình hiện tại, nên không cần restart.</p>
</div>

<div>
<h3>7. Khác nhau giữa <code>setText</code> và <code>setContent</code>?</h3>
<p><code>setText(body)</code> đặt nội dung văn bản thuần và tự đặt MIME type là <code>text/plain</code>.
   <code>setContent(body, "text/html")</code> nhận chuỗi HTML và MIME type <code>text/html</code>, dùng khi gửi email HTML.
   Trang Join cho phép chọn một trong hai định dạng; ta dùng thêm <code>charset=UTF-8</code> để hiển thị đúng tiếng Việt.</p>
</div>

<div>
<h3>8. Cách đặt địa chỉ From, To, CC, BCC, kèm tên, nhiều người nhận?</h3>
<ul>
    <li>From: <code>message.setFrom(new InternetAddress("cds@murach.com"))</code>.</li>
    <li>To / CC / BCC: <code>message.setRecipient(Message.RecipientType.TO | CC | BCC, address)</code>.
        CC = carbon copy (mọi người thấy), BCC = blind carbon copy (người nhận khác không thấy).</li>
    <li>Kèm tên hiển thị: <code>new InternetAddress("andi@yahoo.com", "Andrea Steelman")</code>.</li>
    <li>Nhiều người nhận: truyền mảng <code>Address[]</code> vào <code>setRecipients(...)</code>.</li>
    <li>Thêm vào danh sách đã có: <code>addRecipient</code> / <code>addRecipients</code> (còn <code>setRecipient(s)</code> sẽ ghi đè).</li>
</ul>
<p>Trong app: email chào mừng gửi BCC cho <code>ADMIN_EMAIL</code> (nếu cấu hình); trang Admin gửi 1 thư tới toàn bộ danh sách qua BCC.</p>
</div>

<div>
<h3>9. Gửi thư khi có và không có xác thực?</h3>
<ul>
    <li>Không cần xác thực: phương thức static <code>Transport.send(message)</code>.</li>
    <li>Cần xác thực: <code>Transport t = session.getTransport(); t.connect(user, pass);
        t.sendMessage(message, message.getAllRecipients()); t.close();</code></li>
    <li>Nếu không gửi được, <code>send</code> ném <code>SendFailedException</code> (lớp con của <code>MessagingException</code>).</li>
</ul>
</div>

<div>
<h3>10. Xử lý lỗi khi gửi mail trong servlet thế nào?</h3>
<p>Bọc lời gọi <code>sendMail</code> trong <code>try/catch (MessagingException e)</code>. Khi lỗi: đặt thuộc tính
   <code>errorMessage</code> để JSP hiển thị cho người dùng và ghi đầy đủ email (TO, FROM, SUBJECT, body) vào log bằng
   <code>this.log(...)</code> để quản trị viên có thể gửi lại. Người dùng vẫn được thêm vào danh sách và được chuyển tới <code>thanks.jsp</code>.</p>
</div>

<div>
<h3>11. Cần lưu ý gì về bảo mật khi gửi mail qua Gmail?</h3>
<ul>
    <li>Không hard-code mật khẩu trong mã nguồn như ví dụ <code>connect("johnsmith@gmail.com", "sesame")</code>; project đọc từ biến môi trường
        <code>SMTP_USERNAME</code>/<code>SMTP_PASSWORD</code>.</li>
    <li>Gmail không còn cho đăng nhập SMTP bằng mật khẩu thường; phải bật xác minh 2 bước và tạo <b>App Password</b>.</li>
    <li>Kiểm tra và escape dữ liệu người dùng trước khi đưa vào email HTML/JSP (chống XSS, header injection).</li>
    <li>Nhiều host (vd. Render free) chặn cổng SMTP 25/465/587 &rarr; dùng dịch vụ gửi mail qua HTTPS API (chế độ <code>MAIL_MODE=resend</code>).</li>
</ul>
</div>

<h2>Exercise 14-1 &ndash; đối chiếu</h2>
<table>
    <tr><th>Yêu cầu</th><th>Đã làm</th></tr>
    <tr><td>Servlet gửi email khi user tham gia danh sách</td><td><code>EmailListServlet</code> + <code>WelcomeEmail</code></td></tr>
    <tr><td>Dùng SMTP server cục bộ</td><td><code>MAIL_MODE=local</code> &rarr; <code>MailUtilLocal</code></td></tr>
    <tr><td>Dùng Gmail (SMTP từ xa, có xác thực)</td><td><code>MAIL_MODE=gmail</code> &rarr; <code>MailUtilGmail</code></td></tr>
    <tr><td>Gửi email định dạng HTML</td><td>Chọn &ldquo;HTML (text/html)&rdquo; trên form &rarr; <code>setContent(body, "text/html")</code></td></tr>
    <tr><td>Gửi bản sao (CC/BCC), nhiều người nhận</td><td><code>ADMIN_EMAIL</code> (BCC) và trang <code>/admin</code> (<code>setRecipients</code>)</td></tr>
    <tr><td>Hiển thị lỗi và ghi log khi gửi thất bại</td><td><code>errorMessage</code> trên <code>thanks.jsp</code> + <code>log(...)</code></td></tr>
</table>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
