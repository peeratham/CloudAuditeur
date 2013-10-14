<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<html>
<head>
<title>Auditeur-Lite</title>
</head>

<body>

<p>
<h2>Auditeur-Lite</h2>
</p>

<div>


<c:choose>
	<c:when test="${user != null}">
			<p>
			Welcome, ${user.email}!
			You can <a href="${logoutUrl}">sign out</a>.
			</p>
			<p>
			<a href="/request">Request for model training</a>||
			<a href="/view-models">View Models</a>
			</p>

		<c:choose>
			<c:when test="${hasUploads}">
				<form action="/delete" method="post"> <p>Your uploads:</p>
					<input type="hidden" name="kind" value="soundlet" />
					<ul>
						<c:forEach var="upload" items="${uploads}"> 
						<li>
							<input type="checkbox" name="delete" value="${upload.uploadKey}" />
							${upload.blob.filename} 
							<c:forEach var ="tag" items="${upload.tags}">|${tag}| </c:forEach>
							<a href="/download?key=${upload.uploadKey}&kind=feature">feature file</a>
							
						</li>
						
						</c:forEach>
					</ul>
					<input type="submit" value="Delete Selected" />
				</form> 
			</c:when>
			<c:otherwise>
			<p>You have no uploads.</p>
			</c:otherwise>
		</c:choose>
		<%-- upload form --%>
		<form action="${uploadUrl}" method="post" enctype="multipart/form-data"> 
		<label for="upload">File:</label>
		<input type="file" name="upload" multiple="true" /><br/> 
		<label for="tags">Tags:</label>
		<input type="text" name="tags" id="tags" size="50" /><br/>
		<input type="submit" value="Upload Soundlet" />
		</form> 
		
	</c:when>
	<c:otherwise>
		<p>
		Welcome! Please
		<a href="${loginUrl}">sign in or register</a> to upload files.
		</p>
	</c:otherwise>
</c:choose>

	
	


</div>

</body>
</html>