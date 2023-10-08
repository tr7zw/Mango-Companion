FROM openjdk:17.0-slim

RUN apt update && apt install libxcb-shm0\                                                                
         libx11-xcb1\                                                                            
         libx11-6\                                                                               
         libxcb1\                                                                                
         libxext6\                                                                               
         libxrandr2\                                                                             
         libxcomposite1\                                                                         
         libxcursor1\                                                                            
         libxdamage1\                                                                            
         libxfixes3\                                                                             
         libxi6\                                                                                 
         libxtst6\                                                                               
         libgtk-3-0\                                                                             
         libpangocairo-1.0-0\                                                                    
         libpango-1.0-0\                                                                         
         libatk1.0-0\                                                                            
         libcairo-gobject2\                                                                      
         libcairo2\                                                                              
         libgdk-pixbuf-2.0-0\                                                                    
         libglib2.0-0\                                                                           
         libasound2\                                                                             
         libxrender1\                                                                            
         libfreetype6\                                                                           
         libfontconfig1\                                                                         
         libdbus-glib-1-2\                                                                       
         libdbus-1-3 \
         xvfb \
         -y

WORKDIR /workspace

COPY target/mango-companion-*-jar-with-dependencies.jar /workspace/mango-companion.jar
COPY webapp/style.css ./webapp/style.css
COPY webapp/WEB-INF/web.xml ./webapp/WEB-INF/web.xml

ENV DISPLAY=:0

CMD Xvfb :0 -ac & java -jar /workspace/mango-companion.jar /workspace/library