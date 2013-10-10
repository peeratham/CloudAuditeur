package uva.cs.auditeur.cloud;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ModelTrainingServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		BufferedReader buf = req.getReader();
		String strLine;
		while((strLine = buf.readLine()) != null){
			System.out.println(strLine);
		}
	}
}
