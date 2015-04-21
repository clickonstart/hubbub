package com.grailsinaction

import java.util.Date;

class Post {
	String content
	Date dateCreated

	static constraints = {
		content blank: false
	}
	
	static belongsTo = [ user : User ]
	static hasMany = [ tags : Tag ]
	static mapping = {
		sort dateCreated:"desc"
	}
}