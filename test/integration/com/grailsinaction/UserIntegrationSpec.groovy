package com.grailsinaction

import spock.lang.*

class UserIntegrationSpec extends Specification {
	def "Saving our first user to the database"() {
		
		given: "A brand new user"
	
		def joe = new User(loginId: 'joe', password: 'secret')
		joe.profile = new Profile(fullName: 'joe lastname', homepage: 'http://www.grailsinaction.com', email:'test@test.com')

		when: "the user is saved"
		joe.save()
		
		then: "it saved successfully and can be found in the database"
		joe.errors.errorCount == 0
		joe.id != null
		User.get(joe.id).loginId == joe.loginId
	}
	
	def "Updating a saved user changes its properties"() {
		given: "An existing user"
		def existingUser = new User(loginId: 'joe', password: 'secret')
		existingUser.profile = new Profile(fullName: 'joe lastname', homepage: 'http://www.grailsinaction.com', email:'test@test.com')
		
		existingUser.save(failOnError: true)
		
		when: "A property is changed"
		def foundUser = User.get(existingUser.id)
		foundUser.password = 'sesame'
		foundUser.save(failOnError: true)
		
		then: "The change is reflected in the database"
		User.get(existingUser.id).password == 'sesame'
	}
	
	def "Deleting an existing user removes it from the database"() {
		given: "An existing user"
		def user = new User(loginId: 'joe', password: 'secret')
		user.profile = new Profile(fullName: 'joe lastname', homepage: 'http://www.grailsinaction.com', email:'test@test.com')
		user.save(failOnError: true)
		
		when: "The user is deleted"
		def foundUser = User.get(user.id)
		foundUser.delete(flush: true)
		
		then: "The user is removed from the database"
		!User.exists(foundUser.id)
	}
	
	def "Saving a user with invalid properties causes an error"() {
		given: "A user which fails several field validations"
		def user = new User(loginId: 'joe',	password: 'tiny')
		user.profile = new Profile(fullName: 'joe lastname', homepage: 'not-a-url', email:'test@test.com')
		
		when: "The user is validated"
		user.validate()
		user.profile.validate()
		
		then:
		user.hasErrors()
		user.profile.hasErrors()
		
		"size.toosmall" == user.errors.getFieldError("password").code
		"tiny" == user.errors.getFieldError("password").rejectedValue
		"url.invalid" == user.profile.errors.getFieldError("homepage").code
		"not-a-url" == user.profile.errors.getFieldError("homepage").rejectedValue
		!user.errors.getFieldError("loginId")
	}
	
	def "Recovering from a failed save by fixing invalid properties"() {
		given: "A user that has invalid properties"
		def chuck = new User(loginId: 'chuck', password: 'tiny')
		chuck.profile = new Profile(fullName: 'chuck norris', homepage: 'not-a-url', email:'test@test.com')
		
		assert chuck.save() == null
		assert chuck.hasErrors()
		
		when: "We fix the invalid properties"
		chuck.password = "fistfist"
		chuck.profile.homepage = "http://www.chucknorrisfacts.com"
		chuck.validate()
		
		then: "The user saves and validates fine"
		!chuck.hasErrors()
		chuck.save()
	}
}

