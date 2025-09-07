/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>StandartUnitMachine</H2>
 * <H3>org.schema.schine.ai.stateMachines</H3>
 * StandartUnitMachine.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright © 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.ai.stateMachines;

// TODO: Auto-generated Javadoc

/**
 * this machine controller the basic unit tasks. attacking, defending, refilling
 * ammo, repairing
 *
 * @author schema
 */
public class StandartUnitMachine extends FiniteStateMachine<String> {

	/**
	 * The Constant serialVersionUID.
	 */
	

	/**
	 * Instantiates a new standart unit machine.
	 *
	 * @param obj the obj
	 */
	public StandartUnitMachine(AiEntityState obj) {
		super(obj, null, "");
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.FiniteStateMachine#createFSM()
	 */
	@Override
	public void createFSM(String param) {
		//		Wait t_wait = new Wait();
		//		StartAttack t_mta = new StartAttack();
		//		RotateTurret t_rot = new RotateTurret();
		//		Move t_mov = new Move();
		//		AttackThisUnit t_atu = new AttackThisUnit();
		//		MoveToHelipad t_mth = new MoveToHelipad();
		//		Board t_bord = new Board();
		//		Deboard t_dboa = new Deboard();
		//		PositionReached t_pore = new PositionReached();
		//		ForceMove t_fmov = new ForceMove();
		//		StartBoard t_sboa = new StartBoard();

		//		Waiting wait = new Waiting(getObj());
		//		CheckingWaypoints chkw = new CheckingWaypoints (getObj());
		//		MovingToAttack mota = new MovingToAttack(getObj());
		//		RotatingTurret rota = new RotatingTurret(getObj());
		//		Attacking atta = new Attacking(getObj());
		//		RefillingMunition refi = new RefillingMunition(getObj());
		//		ReturningToHelipad reth = new ReturningToHelipad(getObj());
		//		Boarding boar = new Boarding(getObj());
		//		Deboarding dboa = new Deboarding(getObj());
		//		Boarded bded = new Boarded(getObj());
		//		Moving movi = new Moving(getObj());
		//		ForceMoving fmov = new ForceMoving(getObj());
		//
		//		FSMstate s_wait = new FSMstate(6, wait); // waiting
		//		FSMstate s_move = new FSMstate(8, movi); // moving
		//		FSMstate s_fmov = new FSMstate(8, fmov); // forced moving
		//		FSMstate s_chkw = new FSMstate(3, chkw); // checking for waypoints
		//		FSMstate s_mota = new FSMstate(7, mota); // move to attack
		//		FSMstate s_rota = new FSMstate(7, rota); // rotate turret
		//		FSMstate s_atta = new FSMstate(8, atta); // attacking
		//		FSMstate s_reth = new FSMstate(4, reth); // return to helipad
		//		FSMstate s_refi = new FSMstate(3, refi); // refill Munition
		//		FSMstate s_boar = new FSMstate(7, boar); // boarding
		//		FSMstate s_bded = new FSMstate(2, bded); // boarded
		//		FSMstate s_dboa = new FSMstate(4, dboa); // deboarding

		//
		//		try {
		//			s_chkw.addTransition(t_wait, wait);
		//			s_chkw.addTransition(t_fmov, fmov);
		//			s_chkw.addTransition(t_mov, movi);
		//
		//			s_wait.addTransition(t_mta, mota);
		//			s_wait.addTransition(t_mth, reth);
		//			s_wait.addTransition(t_wait, wait);
		//			s_wait.addTransition(t_sboa, boar);
		//			s_wait.addTransition(t_mov, movi); // waiting -> moving
		//			s_wait.addTransition(t_fmov, fmov);
		//
		//			s_move.addTransition(t_mta, mota);
		//			s_move.addTransition(t_wait, wait);
		//			s_move.addTransition(t_pore, chkw);
		//			s_move.addTransition(t_rot, rota);
		//			s_move.addTransition(t_mth, reth);
		//			s_move.addTransition(t_sboa, boar);
		//			s_move.addTransition(t_mov, movi); // moving -> moving
		//			s_move.addTransition(t_fmov, fmov);
		//
		//			s_fmov.addTransition(t_mta, mota);
		//			s_fmov.addTransition(t_wait, wait);
		//			s_fmov.addTransition(t_pore, chkw);
		//			s_fmov.addTransition(t_rot, rota);
		//			s_fmov.addTransition(t_mth, reth);
		//			s_fmov.addTransition(t_sboa, boar);
		//			s_fmov.addTransition(t_mov, movi); // forced moving -> moving
		//			s_fmov.addTransition(t_fmov, fmov);
		//
		//			s_mota.addTransition(t_wait, wait);
		//			s_mota.addTransition(t_pore, mota); // positionReached -> moving to attack (checks if we are near enough)
		//			s_mota.addTransition(t_rot, rota);
		//			s_mota.addTransition(t_mth, reth);
		//			s_mota.addTransition(t_sboa, boar);
		//			s_mota.addTransition(t_mov, movi); // movingToAttack -> moving
		//			s_mota.addTransition(t_fmov, fmov);
		//
		//			s_rota.addTransition(t_atu, atta); // rotatingTurret -> attackThisRobot
		//			s_rota.addTransition(t_wait, wait);
		//			s_rota.addTransition(t_mth, reth);
		//			s_rota.addTransition(t_mta, mota);
		//			s_rota.addTransition(t_sboa, boar);
		//			s_rota.addTransition(t_mov, movi); // rotatingTurret -> moving
		//			s_rota.addTransition(t_fmov, fmov);
		//
		//			s_atta.addTransition(t_wait, wait);
		//			s_atta.addTransition(t_mta, mota);
		//			s_atta.addTransition(t_atu, atta);
		//			s_atta.addTransition(t_mth, reth);
		//			s_atta.addTransition(t_rot, rota);
		//			s_atta.addTransition(t_sboa, boar);
		//			s_atta.addTransition(t_mov, movi); // rotatingTurret -> moving
		//			s_atta.addTransition(t_fmov, fmov);
		//
		//
		//			s_reth.addTransition(t_wait, wait);
		//			s_reth.addTransition(t_pore, refi);
		//			s_reth.addTransition(t_mta, mota);
		//			s_reth.addTransition(t_mov, movi); // return to helipad -> moving
		//
		//			s_refi.addTransition(t_wait, wait);
		//			s_refi.addTransition(t_mta, mota);
		//			s_refi.addTransition(t_mov, movi); // refill -> moving
		//
		//			s_boar.addTransition(t_sboa, boar);
		//			s_boar.addTransition(t_wait, wait);
		//			s_boar.addTransition(t_pore, boar); //pos reached -> bording (check if near enough)
		//			s_boar.addTransition(t_mta, mota);
		//			s_boar.addTransition(t_bord, bded);
		//			s_boar.addTransition(t_mov, movi); // boarding -> moving
		//			s_boar.addTransition(t_fmov, fmov);
		//
		//			s_bded.addTransition(t_dboa, dboa);
		//			s_bded.addTransition(t_bord, boar); // if not near enough borded -> boarding
		//
		//			s_dboa.addTransition(t_wait, wait);
		//			s_dboa.addTransition(t_mov, movi);
		//			s_dboa.addTransition(t_fmov, fmov);
		//			s_dboa.addTransition(t_bord, bded); //if there is no free field
		//
		//		} catch (MaximumTransitionsException e) {
		//			e.printStackTrace();
		//		}
		//		getFsm().addState(s_wait);
		//		getFsm().addState(s_move);
		//		getFsm().addState(s_mota);
		//		getFsm().addState(s_rota);
		//		getFsm().addState(s_atta);
		//		getFsm().addState(s_reth);
		//		getFsm().addState(s_refi);
		//		getFsm().addState(s_boar);
		//		getFsm().addState(s_bded);
		//		getFsm().addState(s_dboa);
		//		getFsm().addState(s_fmov);
		//		getFsm().addState(s_chkw);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.FiniteStateMachine#onMsg(org.schema.schine.ai.stateMachines.Message)
	 */
	@Override
	public void onMsg(Message message) {
		
	}

}
