<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<html>
<head>
<title>Auditeur-Models</title>
</head>

<body>

<p>
<h2>Auditeur-Models</h2>
</p>

<c:choose>
			<c:when test="${hasModels}">
				<h2>Your Models</h2>
					<ul>
						<c:forEach var="model" items="${models}"> 
						<li>
							<input type="checkbox" name="delete" value="${model.modelKey}" />
							${rangeKey}|${model.lookfor}|${model.within}
							<a href="/download?key=${model.modelKey}">Model file</a>|
						</li>
						</c:forEach>
					</ul>
					<input type="submit" value="Delete Selected" />
			</c:when>
			<c:otherwise>
			<p>You have no models.</p>
			</c:otherwise>
</c:choose>
</body>
</html>

