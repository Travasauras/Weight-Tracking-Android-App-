Sometimes it is hard to hold yourself accountable when it comes to your weight, with this app the accountability is easier to maintain as you track your weight on your journey towards your goal weight.
With the WeightTracker 4000 you can create an account, set a goal weight, and log your weights in where this data will on be stored in a SQLite database on your phone.  Once you create an account you will be 
asked if you would like to opt in to SMS alerts and if you do then you will be sent a congratulatory text once your goal weight has been reached or exceeded. 

During the development of this app, there were 3 screens created, a login screen, the screen where all the data is displayed and entered, as well as an SMS notification screen.  A JAVA CRUD class was used to create,
read, update, and delete information from a SQLite database stored on the user's device.  Java classes were also created to handle the login screen, data screen, and SMS screen where they used both the corresponding xml classes as
well as the CRUD class to store, update, and display data. 

My approach in this app was brainstorming the ideas of what was needed to meet all of the requirements.  Then making a few FlowChart and a Class diagram to figure out the functionality.  After this I sketched out an idea
of what I would like the app to look like.  I grabbed a few open source PNG files to add some graphics and played around with color schemes while designing the screens.  After the screens were created, I began by writing the
CRUD class, then the login class.  Once that was completed I created the SMS class and then the actual main part of app with the data table and weight entries.  Once I had the functionality going I began testing the app
to ensure it worked as required.

All of my testing was user testing done by myself.  I tried it out on a few different emulators to ensure that it would work on multiple devices and tested every function of the app to ensure it worked.  

I don't think there was really anything innovative in my development process though I may have gone a little overboard with toast messages. 

I have always enjoyed SQL, and writing SQL commands to be called with a Java program really was the fun part of this project.  Creating a CRUD class in Java was also neat as I have only done so in python with noSQL, both are
languages I am not as comfortable with as I am with Java and SQL. 
