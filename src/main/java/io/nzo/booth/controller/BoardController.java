package io.nzo.booth.controller;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import de.neuland.jade4j.lexer.token.Comment;
import io.nzo.booth.common.Paging;
import io.nzo.booth.model.Board;
import io.nzo.booth.model.Post;
import io.nzo.booth.model.PostForm;
import io.nzo.booth.service.BoardService;
import io.nzo.booth.service.UserService;

@Controller
@SessionAttributes("user")
public class BoardController
{
	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	
	@Autowired UserService userService;
	@Autowired BoardService boardService;
	
	/**
	 * 목록보기
	 */
//	@RequestMapping(path = "/board/{id}", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_HTML )
//	public String boardMain(Model model, 
//			@PathVariable( name = "id", required = true) String id,
//			@RequestParam( name="page", required = false , defaultValue = 1) int page )
//	{
//		return boardMain(model, id, page);
//	}
	@RequestMapping(path =  "/board/{id}", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_HTML )
	public String main(Model model,
			@PathVariable( name = "id", required = true) String id,
			@RequestParam( name="page", required = false , defaultValue = "1") int page )
	{
		if( page <= 0 ) { page = 1; }
		
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
		
		List<Post> posts = boardService.getPosts(board.getTableNumber(), page, 12);
		model.addAttribute("posts", posts);
		
		Long totalCount = boardService.getPostsTotalCount(board.getTableNumber());
		model.addAttribute("paging", Paging.pagination(totalCount.longValue(), page, 12));
		
		return "board";
	}
	
	/**
	 * 게시물 보기
	 */
	@RequestMapping(path = "/board/{id}/view/{postId}", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_HTML)
	public String postView(Model model, 
			@PathVariable(value = "id", required = true ) String id,
			@PathVariable(value = "postId", required = true) long postId,
			@RequestParam( name="page", required = false , defaultValue = "1") int page)
	{
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
		
		// 게시물
		Post post = boardService.getPost(board.getTableNumber(), postId);
		model.addAttribute("post", post);
		
		// 댓글
		List<Comment> comments = boardService.getComments(board.getTableNumber(), postId);
		model.addAttribute("postComments", comments);
		
		// 이전글 다음글
		model.addAttribute("postUser", userService.getUser(post.getUserId()));
		
		Post nextPost = boardService.getNextPost(board.getTableNumber(), postId);
		Post prevPost = boardService.getPrevPost(board.getTableNumber(), postId);
		model.addAttribute("nextPost", nextPost);
		model.addAttribute("prevPost", prevPost);

		model.addAttribute("page" , page);		// 현재페이지
		
		// 뷰 카운트 갱신
		boardService.setPostIncreaseViewCount(board.getTableNumber(), postId, 1);
		
		return "view";
	}
	
	
	
	/**
	 * 글쓰기
	 */
	@RequestMapping(path = "/board/{id}/write", method = RequestMethod.GET, produces = MediaType.TEXT_HTML)
	public String postWrite(Model model, 
			@PathVariable(value = "id", required = true ) String id,
			@RequestParam( name="page", required = false , defaultValue = "1") int page)
	{
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
		
		model.addAttribute("page", page);
		
		model.addAttribute("postForm", new Post());
		return "write";
	}
	
	@RequestMapping(path = "/board/{id}/write", method = RequestMethod.POST)
	public ModelAndView postAdd(Model model,
			@PathVariable(value = "id", required = true ) String id,
			@ModelAttribute("postForm") Post post)
	{
		boardService.postAdd(id, post.getTitle(), post.getText() );
		ModelAndView mv = new ModelAndView();
		mv.setViewName("redirect:/board/" + id + "?page=1");
		return mv;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
//	// add / modify
	
	// 게시물 보기
	@ResponseBody
	@RequestMapping(path = "/gz/post", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON)
	public String listApi(Model model, 
			@RequestParam(value = "id", required = false, defaultValue="" ) String id,
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
		
		boardService.setPostIncreaseViewCount(board.getTableNumber(), postId, 1);
		
		return new JSONObject(model.asMap()).toString();
	}
	
	
	@ResponseBody
	@RequestMapping(path = "/gz/list", produces = MediaType.APPLICATION_JSON)
	public String listWithApi(Model model, 
			@RequestParam(value = "id", required = false, defaultValue="" ) String id,
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
	@RequestMapping(path = "/gz/board", produces = MediaType.APPLICATION_JSON)
	public String listApi(Model model, 
			@RequestParam(value = "id", required = false, defaultValue="") String id)
	{
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
		
		return new JSONObject(model.asMap()).toString();
	}
	
	
	
	// /board
	// 게시판 정보
	
	// /board/{id}
	// /board/{id}/{page}			= 1페이지 목록
	
	// 글보기 페이지
	// /board/{id}/view/{postId}	=	1번글
	
	// 글쓰기 페이지
	// /board/{id}/write			=	글쓰기 레이아웃
	
	
	// 글쓰기
	// /board/{id}/post/add			=	post 로	
	
	// 글수정
	// /board/{id}/post/{postId}		post 로	
	
	
	
	
	
	// 가입
	// /sign/up

	// 로그인
	// /sign/in
	
	// 로그아웃
	// /sign/out
	
	
	
//	
//	
//	// remove
//	@ResponseBody
//	@RequestMapping(path = "/gz/post", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON )
//	public String setPostRemove(Model model,
//			HttpSession session,
//			@RequestParam(value = "id", required = true ) String id,
//			@RequestParam(value = "title", required = false) String title,
//			@RequestBody(required = false) String text)
//	{
//		if( title.length() > 250 ) {
//			title = title.substring(0, 250);
//		}
//		// TODO 이부분 해결안됨
//		
//		//User user = (User) session.getAttribute("user");
//		boardService.postAdd(id, 0L, 1, false, false, title, text, "", "", "");
//		return new JSONObject().toString();
//	}
//	
//	
//
//	// get
//	@ResponseBody
//	@RequestMapping(path = "/gz/post", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON )
//	public String getPost(Model model,
//			HttpSession session,
//			@RequestParam(value = "id", required = true ) String id,
//			@RequestParam(value = "title", required = false) String title,
//			@RequestBody(required = false) String text)
//	{
//		if( title.length() > 250 ) {
//			title = title.substring(0, 250);
//		}
//		// TODO 이부분 해결안됨
//		
//		//User user = (User) session.getAttribute("user");
//		boardService.postAdd(id, 0L, 1, false, false, title, text, "", "", "");
//		return new JSONObject().toString();
//	}
	
	
	
	
	/**
	 * api code 
	@ResponseBody
	@RequestMapping(path = "/gz/list", produces = MediaType.APPLICATION_JSON)
	public String listWithApi(Model model, 
			@RequestParam(value = "id", required = false, defaultValue="" ) String id,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page)
	{
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
		
		model.addAttribute("posts", boardService.getPosts(board.getTableNumber(), page, 15));
		
		Long totalCount = boardService.getPostsTotalCount(board.getTableNumber());
		model.addAttribute("paging", Paging.pagination(totalCount.longValue(), page, 15));
				
		return new JSONObject(model.asMap()).toString();
	}
	
	 */
	

/**
 *  jade code 
 *  
	@RequestMapping(path = "/board/{id}", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_HTML )
	public String boardMain(Model model, @PathVariable( name = "id", required = true) String id )
	{
	
		// JadeTemplate template = JadeConfig.getTemplate("board");
		//return JadeConfig.renderTemplate(template, model.asMap());
	}
*/
	
	
}
