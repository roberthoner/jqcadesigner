/*
 *  Copyright (c) 2010 Robert Honer <rhoner@cs.ucla.edu>
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the <organization> nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jqcadesigner;

/**
 *
 * @author Robert Honer <rhoner@cs.ucla.edu>
 */
public class JQCADConstants
{
	public static final String		PROGRAM_NAME = "JQCADesigner";
	public static final String		PROGRAM_VERSION = "0.1a";
	public static final String[][]	PROGRAM_AUTHORS = { {"Robert Honer", "rhoner@ucla.edu"} };
	public static final String		PROGRAM_LICENSE = "BSD";

	// Numerical constants
	public static final double QCHARGE						= 1.602176432e-19;
	public static final double QCHARGE_SQRD_OVER_FOUR		= 6.417423538e-39;
	public static final double QCHARGE_HALVED				= 0.801088231e-19;
	public static final double ONE_OVER_QCHARGE				= 6.241509745e18;

	public static final double EPSILON						= 8.8541878e-12;
	public static final double FOUR_PI_EPSILON				= 1.112650056e-10;
	public static final double ONE_OVER_FOUR_HALF_QCHARGE	= 3.12109e18;
}
