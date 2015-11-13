/*******************************************************************
*
*  DESCRIPTION: User Interface for ATM
*
*  AUTHOR: Nan Yang
*
*  EMAIL: yangn1@my.erau.edu
*
*
*  DATE: 10/16/2013
*  This code is hignly based on Dr. Jafer's Atomic Model ScreenSaver
*
*******************************************************************/

#ifndef __UserInterface_H
#define __UserInterface_H

/** include files **/
#include "atomic.h"     // class Atomic
#include "string.h"	// class String

/** declarations **/
class UserInterface : public Atomic
{
public:
	UserInterface( const std::string &name = "UserInterface" ); //Default constructor
	virtual std::string className() const {  return "UserInterface" ;}
	~UserInterface();


protected:

	Model &initFunction();
	Model &externalFunction( const ExternalMessage & );
	Model &internalFunction( const InternalMessage & );
	Model &outputFunction( const InternalMessage & );

private:
	const Port &inCardInfo, &inAmount
	;  //input ports
	Port &outAmount, &outMsg; //output ports
	Time waitTime0;
	Time waitTime1;  //variable that can be accessed and modified from the MA file
	Time waitTime5;
	int amount;



//	int a; //put any variable you will be using in your code!
//  double b;

	enum State{         //to keep current state of the ScreenSaver
			WaitForCardInfo,      //initial state
			Authorization,		  // another sate
			WaitForAmount,
			SendAmount
		};
		State state;

};	// class UserInterface

#endif   //__UserInterface_H
