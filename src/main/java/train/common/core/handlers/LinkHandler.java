package train.common.core.handlers;

import mods.railcraft.api.tracks.RailTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import train.common.api.AbstractTrains;
import train.common.api.EntityRollingStock;

import java.util.List;

public class LinkHandler {

	private World worldObj;

	public LinkHandler(World world) {
		worldObj = world;
	}

	/* coupling cart stuff */
	public void handleStake(EntityRollingStock entityOne, AxisAlignedBB customBoundingBox) {


		if (entityOne.isAttaching) {
			List lis = worldObj.getEntitiesWithinAABBExcludingEntity(entityOne, customBoundingBox.expand(15, 5, 15));
			if (entityOne.bogieFront != null) {
				lis.addAll(worldObj.getEntitiesWithinAABBExcludingEntity(entityOne, entityOne.bogieFront.boundingBox.expand(7, 5, 7)));
			}


			if (lis != null && lis.size() > 0) {
				for (Object ent : lis) {

					/**
					 * first testing if the link can be emptied
					 */
					//if (entityOne instanceof AbstractTrains && entity instanceof AbstractTrains && ((EntityRollingStock)entityOne).updateTicks%10==0) {
					//doesLink1StillExist(entityOne, lis);
					//doesLink2StillExist(entityOne, lis);
					//}

					if (ent instanceof EntityRollingStock && ((EntityRollingStock) ent).isAttaching) {
						addStake((EntityRollingStock) ent, entityOne, true);
					}

				}

			}
		}

			if ( entityOne.cartLinked1 != null) {
				StakePhysic(entityOne.cartLinked1, entityOne, 1);
			}
			if (entityOne.cartLinked2 != null) {
				StakePhysic(entityOne.cartLinked2, entityOne, 2);
			}
	}


	/**
	 * obvious
	 * 
	 * @param entity
	 */
	private void freeLink1(Entity entity) {
		if (entity instanceof EntityRollingStock) {
			((AbstractTrains) entity).Link1 = 0;
			((AbstractTrains) entity).cartLinked1 = null;
			((EntityRollingStock) entity).consist.clear();
			// System.out.println("free link1 "+entity);
		}

	}

	/**
	 * same
	 * 
	 * @param entity
	 */
	private void freeLink2(Entity entity) {
		if (entity instanceof EntityRollingStock) {
			((AbstractTrains) entity).Link2 = 0;
			((AbstractTrains) entity).cartLinked2 = null;
			((EntityRollingStock) entity).consist.clear();
			// System.out.println("free link2 "+entity);
		}
	}


	/**
	 * Attaching to colliding carts
	 */
	public void addStake(EntityRollingStock cart1, EntityRollingStock cart2, boolean byPlayer) {
		if (worldObj.isRemote) {
			return;
		}
			if (cart2.isAttaching && cart1.isAttaching) {


				double d=0;
				double d1=0;

				double distancesX[] = new double[4];
				double distancesZ[] = new double[4];
				double euclidian[] = new double[4];

				distancesX[0] = cart1.posX - cart2.posX ;
				distancesZ[0] = cart1.posZ - cart2.posZ ;
				distancesX[1] = cart1.bogieFront.posX - cart2.posX ;
				distancesZ[1] = cart1.bogieFront.posZ - cart2.posZ ;
				distancesX[2] = cart1.posX - cart2.bogieFront.posX ;
				distancesZ[2] = cart1.posZ - cart2.bogieFront.posZ ;
				distancesX[3] = cart1.bogieFront.posX - cart2.bogieFront.posX ;
				distancesZ[3] = cart1.bogieFront.posZ - cart2.bogieFront.posZ ;

				for(int i = 0; i< distancesX.length;i++){
					euclidian[i] = MathHelper.sqrt_double((distancesX[i] * distancesX[i]) + (distancesZ[i] * distancesZ[i]));
				}

				double minX = euclidian[0];
				int minIndex=0;
				for ( int k=0; k<euclidian.length; k++ )
				{
					if ( Math.abs(euclidian[k]) < Math.abs(minX)){
						minX = euclidian[k];
						minIndex = k;
					}
				}

				d = distancesX[minIndex];
				d1 = distancesZ[minIndex];


				//System.out.println(d2);
				if (MathHelper.sqrt_double((d * d) + (d1 * d1)) <= cart1.getLinkageDistance(cart1)) {
					/**
					 * attach only if the link is free, each cart has two link obviously
					 */
					
					
					
					if (cart1.Link1 == 0 || cart1.Link1 == -1) {
						cart1.Link1 = cart2.getUniqueTrainID();
						//System.out.println(cart1.Link1+" 1 "+cart2.getUniqueTrainID());

					}
					else if (cart1.Link2 == 0 || cart1.Link2 == -1) {
						cart1.Link2 = cart2.getUniqueTrainID();
						//System.out.println(cart1.Link2+" 2 "+cart2.getUniqueTrainID());
					}
					if (cart1.cartLinked1 == null) {
						cart1.cartLinked1 = cart2;
					}
					else if (cart1.cartLinked2 == null) {
						cart1.cartLinked2 = cart2;
					}

					if (cart2.Link1 == 0 || cart2.Link1 == -1) {
						cart2.Link1 = cart1.getUniqueTrainID();
					}
					else if (cart2.Link2 == 0 || cart2.Link2 == -1) {
						cart2.Link2 = cart1.getUniqueTrainID();
					}

					if (cart2.cartLinked1 == null) {
						cart2.cartLinked1 = cart1;
					}
					else if (cart2.cartLinked2 == null) {
						cart2.cartLinked2 = cart1;
					}

					cart2.isAttached = true;

					cart2.isAttaching = false;

					cart1.isAttaching = false;


					cart1.isAttached = true;

					if (cart2.cartLinked1.train != null) {
						EntityRollingStock.allTrains.remove(cart2.cartLinked1.train);
						cart2.cartLinked1.train.getTrains().clear();
						//System.out.println("clearing linked 1");
					}
					if (cart2.cartLinked2 != null && cart2.cartLinked2.train != null) {
						EntityRollingStock.allTrains.remove(cart2.cartLinked2.train);
						cart2.cartLinked2.train.getTrains().clear();
						//System.out.println("clearing linked 2");
					}


					EntityPlayer entityplayer = cart1.worldObj.getClosestPlayerToEntity(cart1, 20);//
					if (entityplayer != null && byPlayer) {
						entityplayer.addChatMessage(new ChatComponentText("attached!"));
					}
				}
			}
	}

	/**
	 * testing if the carts are registered as linked
	 * 
	 * @param cart1
	 * @param cart2
	 * @return
	 */
	public boolean areLinked(Entity cart1, Entity cart2) {
		if (!(cart1 instanceof AbstractTrains) || !(cart2 instanceof AbstractTrains)){
			return false;
		}
		// Debug purpose
		/* System.out.println("cart1.getID() "+ ((AbstractTrains)cart1).getID()); System.out.println("cart2.getID() "+ ((AbstractTrains)cart2).getID()); System.out.println("cart1 link1 " + ((AbstractTrains)cart1).Link1); System.out.println("cart1 link2 " + ((AbstractTrains)cart1).Link2); System.out.println("cart2 link1 " + ((AbstractTrains)cart2).Link1); System.out.println("cart2 link2 " + ((AbstractTrains)cart2).Link2); */
		/**
		 * testing if the ID of the cart1 is registered as attached to the cart2 same with cart2 attached to cart1
		 */
		return ((((AbstractTrains) cart2).getUniqueTrainID() == ((AbstractTrains) cart1).Link1) || (((AbstractTrains) cart2).getUniqueTrainID() == ((AbstractTrains) cart1).Link2)) ||
				((((AbstractTrains) cart1).getUniqueTrainID() == ((AbstractTrains) cart2).Link1) || (((AbstractTrains) cart1).getUniqueTrainID() == ((AbstractTrains) cart2).Link2));
	}

	/**
	 * getting the optimal distance for each cart
	 * 
	 * @param cart1
	 * @param cart2
	 * @return
	 */
	public float getOptimalDistance(AbstractTrains cart1, AbstractTrains cart2) {
		return cart1.getOptimalDistance(cart2)+cart2.getOptimalDistance(cart1);
	}

	/**
	 * testing if the cart can pull or not
	 * 
	 * @param cart1
	 * @param cart2
	 * @return
	 */
	private boolean canCartBeAdjustedBy(Entity cart1, Entity cart2) {
		if (cart1 == cart2) {
			return false;
		}
		// System.out.println("cart "+cart1+"  cart adjusted "+((EntityRollingStock)cart1).canBeAdjusted((EntityMinecart)cart2));
		if (((cart1 instanceof EntityRollingStock)) && (!((EntityRollingStock) cart1).canBeAdjusted((EntityMinecart) cart2))) {
			return false;
		}
		return !RailTools.isCartLockedDown((EntityMinecart) cart1);
	}

	/**
	 * Handles the cart coupling physics
	 */
	private void StakePhysic(AbstractTrains cart1, AbstractTrains cart2, int linkIndex) {
		if (worldObj.isRemote || cart1.ticksExisted<5 || cart2.ticksExisted<5) {
			return;
		}
		if (cart2.isAttached && cart1.isAttached && areLinked(cart2, cart1)) {


			boolean adj1 = canCartBeAdjustedBy(cart1, cart2);
			boolean adj2 = canCartBeAdjustedBy(cart2, cart1);

			double d=0;
			double d1=0;
			double vecX=0;
			double vecZ=0;
			int minIndex=0;


			double distancesX[] = new double[4];
			double distancesZ[] = new double[4];
			double euclidian[] = new double[4];

			distancesX[0] = cart1.posX - cart2.posX;
			distancesZ[0] = cart1.posZ - cart2.posZ;
			distancesX[1] = cart1.bogieFront.posX - cart2.posX ;
			distancesZ[1] = cart1.bogieFront.posZ - cart2.posZ ;
			distancesX[2] = cart1.posX - cart2.bogieFront.posX ;
			distancesZ[2] = cart1.posZ - cart2.bogieFront.posZ ;
			distancesX[3] = cart1.bogieFront.posX - cart2.bogieFront.posX ;
			distancesZ[3] = cart1.bogieFront.posZ - cart2.bogieFront.posZ ;

			for(int i = 0; i< distancesX.length;i++){
				euclidian[i] = MathHelper.sqrt_double(((distancesX[i]) * (distancesX[i])) + ((distancesZ[i]) * (distancesZ[i])));
			}
			double minX = euclidian[0];
			for ( int k=0; k<euclidian.length; k++ )
			{
				if ( Math.abs(euclidian[k]) < Math.abs(minX)){
					minX = euclidian[k];
					minIndex = k;
				}
			}

			d = distancesX[minIndex];
			d1 = distancesZ[minIndex];
			vecX=d;
			vecZ=d1;


			double d2 = MathHelper.sqrt_double((d * d) + (d1 * d1));
			if(d2>20){
				if(cart1.worldObj!=null){
					EntityPlayer player = cart1.worldObj.getClosestPlayer(cart1.posX, cart1.posY, cart1.posZ, 300);
					if(player!=null){
						player.addChatMessage(new ChatComponentText(String.format("[TRAINCRAFT] The rolling stock at %d %d %d had a problem loading and has lost its link. Attached cart was too far away", (int)cart1.posX, (int)cart1.posY, (int)cart1.posZ)));
					}
				}

				if(linkIndex==1){
					this.freeLink1(cart1);
					this.freeLink1(cart2);
				}
				if(linkIndex==2){
					this.freeLink2(cart1);
					this.freeLink2(cart2);
				}
				return;
			}
			double vecNorm = MathHelper.sqrt_double(vecX * vecX + vecZ * vecZ);

			double unitX = vecX / vecNorm;
			double unitZ = vecZ / vecNorm;

			double stretch = d2 -getOptimalDistance(cart1, cart2);
			//System.out.println("stretch "+stretch);

			double springX = limitForce(0.1D * stretch * vecX * -1);
			double springZ = limitForce(0.1D * stretch * vecZ * -1);



			if (adj1) {
				cart1.motionX += springX;
				cart1.motionZ += springZ;
			}
			if (adj2) {
				cart2.motionX -= springX;
				cart2.motionZ -= springZ;
			}

			double dot = (cart1.motionX - cart2.motionX) * unitX + (cart1.motionZ - cart2.motionZ) * unitZ;

			double dampX = limitForce(0.4D * dot * unitX * -1);// 0.4
			double dampZ = limitForce(0.4D * dot * unitZ * -1);

			if (adj1) {
				cart1.motionX += dampX;
				cart1.motionZ += dampZ;
			}
			if (adj2) {
				cart2.motionX -= dampX;
				cart2.motionZ -= dampZ;
			}
		}
	}

	private static double limitForce(double force) {
		return Math.copySign(Math.abs(Math.min(Math.abs(force), 14.0D)), force);
	}
}