<%@ page language="java"  contentType="text/html; charset=UTF-8" %>
<html>
<body>
<h2>Hello World!</h2>

SpringMVC上传文件
<form name="form1" action="/mmallx_war/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="SpringMVC上传文件" />
</form>

富文本上传文件
<form name="form2" action="/mmallx_war/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="富文本上传文件" />
</form>
</body>
</html>
