package train.common.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.items.ItemWrench;
import train.common.library.EnumTracks;
import train.common.library.Info;
import train.common.tile.TileTCRail;
import train.common.tile.TileTCRailGag;

import java.util.Arrays;
import java.util.Random;

public class BlockTCRail extends Block {
	private IIcon texture;

	public BlockTCRail() {
		super(Material.iron);
		setCreativeTab(Traincraft.tcTab);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0F, 1.0F);
	}

	/**
	 * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
	 */
	@Override
	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
		return false;
	}


	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)  {
		TileTCRail tileEntity = (TileTCRail) world.getTileEntity(x, y, z);
		if (tileEntity != null && tileEntity.idDrop != null) {
			return new ItemStack(tileEntity.idDrop);
		}
		return null;
	}

	@Override
	public int quantityDropped(Random random) {
		return 0;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}
	private static final int[] matrixXZ = {0,-1,-2,1,2}, matrixY = {0,-1,-2,1,2};

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		TileEntity tileEntity = world.getTileEntity(x,y,z);
		if (tileEntity instanceof TileTCRailGag) {
			tileEntity = world.getTileEntity(((TileTCRailGag)tileEntity).originX.get(0), ((TileTCRailGag)tileEntity).originY.get(0), ((TileTCRailGag)tileEntity).originZ.get(0));
		}
		if(tileEntity instanceof TileTCRail){
			((TileTCRail)tileEntity).lastPlayerToInteract = player;
		}


    }

	@Override
	public void breakBlock(World world, int i, int j, int k, Block par5, int par6) {
		TileTCRail tileEntity = (TileTCRail) world.getTileEntity(i, j, k);
		if (tileEntity != null && tileEntity.isLinkedToRail) {
			// NOTE: func_147480_a = destroyBlock
			world.func_147480_a(tileEntity.linkedX, tileEntity.linkedY, tileEntity.linkedZ, false);
			world.removeTileEntity(tileEntity.linkedX, tileEntity.linkedY, tileEntity.linkedZ);
		}
		if (tileEntity != null && (tileEntity.idDrop != null) && !world.isRemote) {
			EntityPlayer player = tileEntity.lastPlayerToInteract;
			if (player != null && !player.capabilities.isCreativeMode) {
				this.dropBlockAsItem(world, i, j, k, new ItemStack(tileEntity.idDrop, 1, 0));
			}
		}
		for(int x : matrixXZ){
			for(int z : matrixXZ){
				for(int y : matrixY){
					if (tileEntity != null && world.getBlock(x + tileEntity.xCoord, y + tileEntity.yCoord, z + tileEntity.zCoord)instanceof BlockTCRailGag){
						if(((TileTCRailGag)world.getTileEntity(x + tileEntity.xCoord, y + tileEntity.yCoord, z + tileEntity.zCoord)).originX.size()>1){
							((TileTCRailGag)world.getTileEntity(x + tileEntity.xCoord, y + tileEntity.yCoord, z + tileEntity.zCoord)).originX.removeAll(Arrays.asList(new int[]{tileEntity.xCoord}));
							((TileTCRailGag)world.getTileEntity(x + tileEntity.xCoord, y + tileEntity.yCoord, z + tileEntity.zCoord)).originY.removeAll(Arrays.asList(new int[]{tileEntity.yCoord}));
							((TileTCRailGag)world.getTileEntity(x + tileEntity.xCoord, y + tileEntity.yCoord, z + tileEntity.zCoord)).originY.removeAll(Arrays.asList(new int[]{tileEntity.zCoord}));
						} else {
							world.notifyBlockChange((x + tileEntity.xCoord), (y + tileEntity.yCoord + 1), (z + tileEntity.zCoord), Blocks.air);
							world.markBlockForUpdate((x + tileEntity.xCoord), (y + tileEntity.yCoord + 1), (z + tileEntity.zCoord));
						}
					}
					if (tileEntity != null && world.getBlock(x + tileEntity.xCoord, y + tileEntity.yCoord, z + tileEntity.zCoord)instanceof BlockTCRail){
						world.notifyBlockChange((x  + tileEntity.xCoord), (y + tileEntity.yCoord + 1), (z  + tileEntity.zCoord), Blocks.air);
						world.markBlockForUpdate((x  + tileEntity.xCoord), (y + tileEntity.yCoord + 1 ), (z  + tileEntity.zCoord));
					}
				}
			}
		}

		world.removeTileEntity(i, j, k);
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, Block par5) {
		TileEntity tile = world.getTileEntity(i, j, k);
		if (tile == null || !(tile instanceof TileTCRail))
			return;

		TileTCRail tileEntity = (TileTCRail) world.getTileEntity(i, j, k);
		if (tileEntity != null && tileEntity.isLinkedToRail) {
			if (world.isAirBlock(tileEntity.linkedX, tileEntity.linkedY, tileEntity.linkedZ)) {
				// NOTE: func_147480_a = destroyBlock
				world.removeTileEntity(i, j, k);
				world.func_147480_a(i, j, k, false);
			}
		}
		if (!World.doesBlockHaveSolidTopSurface(world, i, j - 1, k) && world.getBlock(i, j-1, k) != TCBlocks.bridgePillar) {
			// NOTE: func_147480_a = destroyBlock
			world.func_147480_a(i, j, k, false);
			world.removeTileEntity(i, j, k);
		}
		if (tileEntity != null && !world.isRemote) {
			boolean flag = world.isBlockIndirectlyGettingPowered(i, j, k);
			if (tileEntity.previousRedstoneState != flag) {
				tileEntity.changeSwitchState(world, tileEntity, i, j, k);
				tileEntity.previousRedstoneState = flag;
			}
		}
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileTCRail();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9) {
		TileEntity te = world.getTileEntity(i, j, k);
		int l = world.getBlockMetadata(i, j, k);
		if (!world.isRemote && te != null && (te instanceof TileTCRail)) {
			if (player != null && player.inventory != null && player.inventory.getCurrentItem() != null && (player.inventory.getCurrentItem().getItem() instanceof ItemWrench) && ((TileTCRail) te).getType() != null && ((TileTCRail) te).getType().equals( EnumTracks.SMALL_STRAIGHT.getLabel())) {
				l++;
				if (l > 3)
					l = 0;
				world.setBlockMetadataWithNotify(i, j, k, l, 2);
				((TileTCRail) te).hasRotated = true;
				return true;
			}
			//((TileTCRail)te).printInfo();
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		texture = iconRegister.registerIcon(Info.modID.toLowerCase() + ":tracks/rail_normal_turned");
	}

	@Override
	public IIcon getIcon(int i, int j) {
		return texture;
	}


	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {

		return world==null ? AxisAlignedBB.getBoundingBox(i -18f, j, k -18f, i +18f, j, k +18f)
		: AxisAlignedBB.getBoundingBox(i + this.minX , j + this.minY , k + this.minZ , i + maxX, j + this.maxY , k + this.maxZ);


	}
}
