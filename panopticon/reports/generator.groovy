name "Generator Performance"

chart("Method Average Times") { 
	metric('avg') 
}

chart("All Times Averaged") { 
	average( 	
			metric('avg') 
	)
}

chart("Max Time") { 
	max( 	
			metric('avg') 
	)
}

chart("Min Time") { 
	min( 	
			metric('avg') 
	)
}


