# bank-app

# run consul-agent
docker run -d --name consul -p 8500:8500 consul:1.15.4 agent -dev -client=0.0.0.0 
