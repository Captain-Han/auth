package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import com.mongodb.casbah.WriteConcern
import se.radley.plugin.salat.Binders._

object Blogs extends Controller {

   val blogForm : Form[Blog]= Form(
      mapping(
      "title" -> text,
      "content" -> text)
      {(title, content) => Blog(new ObjectId, title, content)}
      {blog => Some((blog.title, blog.content))}
      )

/* def showBlog() = Action {
    val blogList = Blog.findAll()
    Ok(views.html.showBlog(blogList))
  }*/
  
  
   /**
   * 创建blog，跳转
   */
  def newBlog() = Action {
    Ok(views.html.blog(blogForm))
  }
  
   /**
   * 新建blog，后台逻辑
   */
  def writeBlog() = Action {
    implicit request =>
      blogForm.bindFromRequest.fold(
        //处理错误        
        errors => BadRequest(views.html.message(errors)),
        {
          blog =>
            Blog.save(blog, WriteConcern.Safe)
            Ok(views.html.blogDetail(blogForm.fill(blog)))
        }
        )
  }  
  
   /**
   * 编辑blog
   */
  /*def editBlog(blogId : ObjectId) = Action {
    val blog = Blog.findOneById(blogId)
    blog.map { blog =>
      val formEditBlog = blogForm().fill(blog)
//      Ok(views.html.blog.admin.editBlog(formEditBlog, list, user, blog))
      Ok(views.html.editBlog(formEditBlog))
    } getOrElse {
      NotFound
    }
  }*/
  
   /**
   * 编辑blog，后台逻辑
   */
 /* def modBlog(blogId : ObjectId) = Action {
    implicit request =>
      blogForm.bindFromRequest.fold(
        //处理错误        
        errors => BadRequest(views.html.errorMsg(errors)),
        {
          blog =>            
            Blog.save(blog, WriteConcern.Safe)
            Redirect(routes.Blogs.showBlogById(blogId))
        }             
        )
  }*/
   /**
   * 显示某一条blog
   * 通过blog的id找到blog
   */
  /*def showBlogById(blogId: ObjectId) = Action {
    val blog = Blog.findOneById(blogId).get
    Ok(views.html.blogDetail(blog))
  }*/
}
