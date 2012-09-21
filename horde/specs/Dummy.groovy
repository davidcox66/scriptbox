Random rand = new Random();

addAction( 'foo1', [
	init: {
		println "Foo1: Init called";
	},
	pre: {
		println "Foo1: Pre called";
		return true;
	},
	run: {
		println "Foo1: Run called";
		Thread.sleep( 300 + rand.nextInt(1000) );
		return true;
    },
	post: {
		println "Foo1: Post called";
	}
] );	

addAction( 'foo2', [
	init: {
		println "Foo2: Init called";
	},
	pre: {
		println "Foo2: Pre called";
		return true;
	},
	run: {
		println "Foo2: Run called";
		Thread.sleep( 300 + rand.nextInt(1000) );
		return true;
    },
	post: {
		println "Foo2: Post called";
	}
] );	

destroy( {
	println "destroy1 called"
} );

destroy( {
	println "destroy2 called"
} );
