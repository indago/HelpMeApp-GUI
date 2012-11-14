package com.indago.helpme.gui.dashboard.statemachine;

/**
 * 
 * @author martinmajewski
 * 
 */
public interface IStateAwareView {

	public void setState(IStateMachine stateMachine);

	public Enum<?> getState();

}
