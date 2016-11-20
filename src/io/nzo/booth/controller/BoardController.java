package io.nzo.booth.controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import de.neuland.jade4j.lexer.token.Comment;
import de.neuland.jade4j.template.JadeTemplate;
import io.nzo.booth.JadeConfig;
import io.nzo.booth.common.Paging;
import io.nzo.booth.model.Board;
import io.nzo.booth.model.Post;
import io.nzo.booth.model.User;
import io.nzo.booth.service.BoardService;
import io.nzo.booth.service.UserService;

@Controller
@SessionAttributes("user")
public class BoardController
{
	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	
	@Autowired
	UserService userService;
	
	@Autowired
	BoardService boardService;
	
	@ResponseBody
	@RequestMapping(path = "/board", produces = MediaType.TEXT_HTML)
	public String board(Model model) 
	{
		JadeTemplate template = JadeConfig.getTemplate("board");
		return JadeConfig.renderTemplate(template, model.asMap());
	}
	
	@ResponseBody
	@RequestMapping(path = "/e/list", produces = MediaType.TEXT_HTML)
	public String list(Model model) 
	{
		JadeTemplate template = JadeConfig.getTemplate("list");
		return JadeConfig.renderTemplate(template, model.asMap());
	}
	
	@ResponseBody
	@RequestMapping(path = "/e/view", produces = MediaType.TEXT_HTML)
	public String view(Model model)
	{
		JadeTemplate template = JadeConfig.getTemplate("view");
		return JadeConfig.renderTemplate(template, model.asMap());
	}
	
	@ResponseBody
	@RequestMapping(path = "/e/write", produces = MediaType.TEXT_HTML)
	public String write(Model model)
	{
		JadeTemplate template = JadeConfig.getTemplate("write");
		return JadeConfig.renderTemplate(template, model.asMap());
	}
	
	
	@ResponseBody
	@RequestMapping(path = "/gz/postAdd", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON )
	public String addPost(Model model,
			HttpSession session,
			@RequestParam(value = "id", required = true ) String id,
			@RequestParam(value = "title", required = false) String title,
			@RequestBody(required = false) String text)
	{
		if( title.length() > 250 ) {
			title = title.substring(0, 250);
		}
		// TODO 이부분 해결안됨
		
		//User user = (User) session.getAttribute("user");
		boardService.postAdd(id, 0L, 1, false, false, title, text, "", "", "");
		return new JSONObject().toString();
	}
	
	
	@ResponseBody
	@RequestMapping(path = "/gz/list", produces = MediaType.APPLICATION_JSON)
	public String listWithApi(Model model, 
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page)
	{
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
		
		model.addAttribute("posts", boardService.getPosts(board.getTableNumber(), page, 15));
		
		Long totalCount = boardService.getPostsTotalCount(board.getTableNumber());
		model.addAttribute("paging", Paging.pagination(totalCount.longValue(), page, 15));
		
		return new JSONObject(model.asMap()).toString();
	}
	
	@ResponseBody
	@RequestMapping(path = "/gz/view", produces = MediaType.APPLICATION_JSON)
	public String listApi(Model model, 
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "postId", required = true) long postId)
	{
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
		
		Post post = boardService.getPost(board.getTableNumber(), postId);
		
		model.addAttribute("post", post);
		
		List<Comment> comments = boardService.getComments(board.getTableNumber(), postId);
		model.addAttribute("postComments", comments);
		
		// user
		model.addAttribute("postUser", userService.getUser(post.getUserId()));
		
		model.addAttribute("nextPost", boardService.getNextPost(board.getTableNumber(), postId));
		model.addAttribute("prevPost", boardService.getPrevPost(board.getTableNumber(), postId));
		
		return new JSONObject(model.asMap()).toString();
	}
	
	
	
	
	@ResponseBody
	@RequestMapping(path = "/gz/board", produces = MediaType.APPLICATION_JSON)
	public String listApi(Model model, 
			@RequestParam(value = "id", required = true) String id)
	{
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
		
		return new JSONObject(model.asMap()).toString();
	}
	
	
	
	
	
	
	
	
	
	
}
