// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.util.*;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;

/** Servlet that returns comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

	private static final Gson gson = new Gson();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

		List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String comment = (String) entity.getProperty("comment");
      String timestamp = (String) dateFormat.format(entity.getProperty("timestamp"));

      Comment commentObj = new Comment(id, comment, timestamp);
      comments.add(commentObj);
    }
		response.setContentType("application/json;");
		response.getWriter().println(commentsToJson(comments));
  }

  @Override 
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String comment = request.getParameter("comment-text");
		Date timestamp = new Date();
		addComment(comment, timestamp);
		response.sendRedirect("/index.html");
  }

	private String commentsToJson(List<Comment> comments) {
		return gson.toJson(comments);
	}

	private void addComment(String comment, Date timestamp) {
		Entity commentEntity = new Entity("Comment");
		commentEntity.setProperty("comment", comment);
		commentEntity.setProperty("timestamp", timestamp);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
	}
}
