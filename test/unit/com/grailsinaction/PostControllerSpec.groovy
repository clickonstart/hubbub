package com.grailsinaction

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(PostController)
@Mock([User,Post])
class PostControllerSpec extends Specification {
	def "Get a users timeline given their id"() {
		given: "A user with posts in the db"
		User chuck = new User(
			loginId: "chuck_norris",
			password: "password")
		chuck.addToPosts(new Post(content: "A first post"))
		chuck.addToPosts(new Post(content: "A second post"))
		chuck.save(failOnError: true)
		
		and: "A loginId parameter"
		params.id = chuck.loginId
		
		when: "the timeline is invoked"
		def model = controller.timeline()
		
		then: "the user is in the returned model"
		model.user.loginId == "chuck_norris"
		model.user.posts.size() == 2
	}
	
	def "Check that non-existent users are handled with an error"() {
		given: "the id of a non-existent user"
		params.id = "this-user-id-does-not-exist"
		
		when: "the timeline is invoked"
		controller.timeline()
		
		then: "a 404 is sent to the browser"
		response.status == 404
	}
	
	/* Prior to service impl
	def "Adding a valid new post to the timeline"() {
		given: "A user with posts in the db"
		User chuck = new User(
			loginId: "chuck_norris",
			password: "password").save(failOnError: true)
			
		and: "A loginId parameter"
		params.id = chuck.loginId
		
		and: "Some content for the post"
		params.content = "Chuck Norris can unit test entire applications with a single assert."
		
		when: "addPost is invoked"
		def model = controller.addPost()
		
		then: "our flash message and redirect confirms the success"
		flash.message == "Successfully created Post"
		response.redirectedUrl == "/post/timeline/${chuck.loginId}"
		Post.countByUser(chuck) == 1
	}
	*/
	
    def "Adding an invalid new post to the timeline"() {
        given: "A user with posts in the db"
        User chuck = new User(loginId: "chuck_norris", password: "password").save(failOnError: true)

        and: "A post service that throws an exception with the given data"
        def errorMsg = "Invalid or empty post"
        def mockPostService = Mock(PostService)
        controller.postService = mockPostService
        1 * mockPostService.createPost(chuck.loginId, null) >> { throw new PostException(message: errorMsg) }

        and: "A loginId parameter"
        params.id = chuck.loginId

        and: "Some content for the post"
        params.content = null

        when: "addPost is invoked"
        def model = controller.addPost()

        then: "our flash message and redirect confirms the success"
        flash.message == errorMsg
        //response.redirectedUrl == "/users/${chuck.loginId}"
        Post.countByUser(chuck) == 0

        // Without the custom URL mapping, the check would be this:
		response.redirectedUrl == "/post/timeline/${chuck.loginId}"
    }
	
	@spock.lang.Unroll
	def "Testing id of #suppliedId redirects to #expectedUrl"() {
		given:
		params.id = suppliedId
	
		when: "Controller is invoked"
		controller.home()
	
		then:
		response.redirectedUrl == expectedUrl
	
		where:
		suppliedId | expectedUrl
		'joe_cool' | '/post/timeline/joe_cool'
		null | '/post/timeline/chuck_norris'
	}
	
	def "Adding a valid new post to the timeline"() {
		given: "a mock post service"
		def mockPostService = Mock(PostService)
		1 * mockPostService.createPost(_, _) >>  new Post(content: "Mock Post")
		controller.postService = mockPostService
		
		when: "controller is invoked"
		def result = controller.addPost("joe_cool", "Posting up a storm")
		
		then: "redirected to timeline, flash message tells us all is well"
		flash.message ==~ /Added new post: Mock.*/
		response.redirectedUrl == '/post/timeline/joe_cool'
	}
}