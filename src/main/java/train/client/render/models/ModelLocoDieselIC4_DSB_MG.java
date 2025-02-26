package train.client.render.models;
//Exported java file
//Keep in mind that you still need to fill in some blanks
// - ZeuX

import fexcraft.tmt.slim.ModelRendererTurbo;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import fexcraft.tmt.slim.ModelBase;
import train.common.core.util.TraincraftUtil;

public class ModelLocoDieselIC4_DSB_MG extends ModelBase
{
	//fields
	ModelRendererTurbo Left1;
	ModelRendererTurbo Left2;
	ModelRendererTurbo Left3;
	ModelRendererTurbo Floor1;
	ModelRendererTurbo Floor2;
	ModelRendererTurbo Floor_3;
	ModelRendererTurbo Floor_4;
	ModelRendererTurbo Floor_5;
	ModelRendererTurbo Ceiling;
	ModelRendererTurbo Right1;
	ModelRendererTurbo Right2;
	ModelRendererTurbo Right3;
	ModelRendererTurbo Cab_2;
	ModelRendererTurbo Cab_3;
	ModelRendererTurbo Cab_4;
	ModelRendererTurbo Cab_5;
	ModelRendererTurbo Cab_6;
	ModelRendererTurbo Cab_7;
	ModelRendererTurbo Cab_8;
	ModelRendererTurbo Cab_9;
	ModelRendererTurbo Cab_10;
	ModelRendererTurbo Cab_11;
	ModelRendererTurbo Cab_12;
	ModelRendererTurbo Cab_13;
	ModelRendererTurbo Cab_14;
	ModelRendererTurbo Cab_15;
	ModelRendererTurbo Cab_16;
	ModelRendererTurbo Cab_17;
	ModelRendererTurbo Cab_18;
	ModelRendererTurbo Cab_19;
	ModelRendererTurbo Cab_20;
	ModelRendererTurbo Cab_21;
	ModelRendererTurbo Cab_22;
	ModelRendererTurbo Cab_23;
	ModelRendererTurbo Cab_24;
	ModelRendererTurbo Cab_25;
	ModelRendererTurbo Cab_26;
	ModelRendererTurbo Cab_27;
	ModelRendererTurbo Cab_28;
	ModelRendererTurbo Cab_29;
	ModelRendererTurbo Cab_30;
	ModelRendererTurbo Cab_31;
	ModelRendererTurbo Coupler_1;
	ModelRendererTurbo Coupler_2;
	ModelRendererTurbo ControlPanel1;
	ModelRendererTurbo ControlPanel2;
	ModelRendererTurbo ControlPanel3;
	ModelRendererTurbo ControlPanel4;
	ModelRendererTurbo ControlPanel5;
	ModelRendererTurbo ControlPanel6;
	ModelRendererTurbo WallToCab;
	ModelRendererTurbo MiddleWall1;
	ModelRendererTurbo MiddleWall2;
	ModelRendererTurbo BackWall;
	ModelRendererTurbo DriverChair1;
	ModelRendererTurbo DriverChair2;
	ModelRendererTurbo DriverChair3;
	ModelRendererTurbo Wheels1;
	ModelRendererTurbo Wheels2;
	ModelRendererTurbo Seats1;
	ModelRendererTurbo Seats2;
	ModelRendererTurbo Seats3;
	ModelRendererTurbo Seats4;
	ModelRendererTurbo Seats5;
	ModelRendererTurbo Seats6;
	ModelRendererTurbo Seats7;
	ModelRendererTurbo Seats8;
	ModelRendererTurbo Seats9;
	ModelRendererTurbo Seats10;
	ModelRendererTurbo Seats11;
	ModelRendererTurbo Seats12;
	ModelRendererTurbo Seats13;
	ModelRendererTurbo Seats14;
	ModelRendererTurbo Seats15;
	ModelRendererTurbo Seats16;
	ModelRendererTurbo Seats17;
	ModelRendererTurbo Seats18;
	ModelRendererTurbo Seats19;
	ModelRendererTurbo Seats20;
	ModelRendererTurbo Seats21;
	ModelRendererTurbo Seats22;
	ModelRendererTurbo Seats23;
	ModelRendererTurbo Seats24;
	ModelRendererTurbo Seats25;
	ModelRendererTurbo Seats26;
	ModelRendererTurbo Seats27;
	ModelRendererTurbo Seats28;
	ModelRendererTurbo Seats29;
	ModelRendererTurbo Seats30;
	ModelRendererTurbo Seats31;
	ModelRendererTurbo Seats32;
	ModelRendererTurbo Seats33;
	ModelRendererTurbo Seats34;
	ModelRendererTurbo Seats35;
	ModelRendererTurbo Seats36;
	ModelRendererTurbo Seats37;
	ModelRendererTurbo Seats38;
	ModelRendererTurbo Seats39;
	ModelRendererTurbo Seats40;
	ModelRendererTurbo Seats41;
	ModelRendererTurbo Seats42;
	ModelRendererTurbo Seats43;
	ModelRendererTurbo Seats44;
	ModelRendererTurbo Seats45;
	ModelRendererTurbo Seats46;
	ModelRendererTurbo Seats47;
	ModelRendererTurbo Seats48;
	ModelRendererTurbo Right4;
	ModelRendererTurbo Left4;
	ModelRendererTurbo Cab_32;
	ModelRendererTurbo Cab_33;
	ModelRendererTurbo Bottom;
	ModelRendererTurbo Showel5;
	ModelRendererTurbo Showel6;
	ModelRendererTurbo Showel7;
	ModelRendererTurbo Showel8;
	ModelRendererTurbo Showel9;
	ModelRendererTurbo Shape1;

	public ModelLocoDieselIC4_DSB_MG()
	{
		textureWidth = 256;
		textureHeight = 256;

		Left1 = new ModelRendererTurbo(this, 202, 0);
		Left1.addBox(0F, 0F, 0F, 2, 110, 6);
		Left1.setRotationPoint(9F, 18F, 0F);
		Left1.setTextureSize(256, 256);
		Left1.mirror = true;
		setRotation(Left1, 1.570796F, 0F, 0.2230717F);
		Left2 = new ModelRendererTurbo(this, 111, 0);
		Left2.addBox(0F, 0F, 0F, 2, 100, 15);
		Left2.setRotationPoint(10.1F, 13F, 10F);
		Left2.setTextureSize(256, 256);
		Left2.mirror = true;
		setRotation(Left2, 1.570796F, 0F, 0F);
		Left3 = new ModelRendererTurbo(this, 146, 0);
		Left3.addBox(0F, 0F, 0F, 2, 100, 6);
		Left3.setRotationPoint(10.25F, -1F, 10F);
		Left3.setTextureSize(256, 256);
		Left3.mirror = true;
		setRotation(Left3, 1.570796F, 0F, -0.2230717F);
		Floor1 = new ModelRendererTurbo(this, 0, 0);
		Floor1.addBox(0F, 0F, 0F, 20, 111, 1);
		Floor1.setRotationPoint(-10F, 18F, -1F);
		Floor1.setTextureSize(256, 256);
		Floor1.mirror = true;
		setRotation(Floor1, 1.570796F, 0F, 0F);
		Floor2 = new ModelRendererTurbo(this, 25, 125);
		Floor2.addBox(0F, 0F, 0F, 20, 111, 1);
		Floor2.setRotationPoint(-9F, 14F, -1F);
		Floor2.setTextureSize(256, 256);
		Floor2.mirror = true;
		setRotation(Floor2, 1.570796F, 0F, 0F);
		Floor_3 = new ModelRendererTurbo(this, 122, 225);
		Floor_3.addBox(0F, 0F, 0F, 1, 19, 11);
		Floor_3.setRotationPoint(10F, 13F, -12F);
		Floor_3.setTextureSize(256, 256);
		Floor_3.mirror = true;
		setRotation(Floor_3, 0F, 0F, 1.570796F);
		Floor_4 = new ModelRendererTurbo(this, 148, 226);
		Floor_4.addBox(0F, 0F, 0F, 1, 17, 11);
		Floor_4.setRotationPoint(9F, 17F, -12F);
		Floor_4.setTextureSize(256, 256);
		Floor_4.mirror = true;
		setRotation(Floor_4, -0.0371786F, 0F, 1.570796F);
		Floor_5 = new ModelRendererTurbo(this, 174, 230);
		Floor_5.addBox(0F, 0F, 0F, 1, 15, 9);
		Floor_5.setRotationPoint(-7F, 18F, -21F);
		Floor_5.setTextureSize(256, 256);
		Floor_5.mirror = true;
		setRotation(Floor_5, 0F, 0F, -1.570796F);
		Ceiling = new ModelRendererTurbo(this, 163, 0);
		Ceiling.addBox(0F, 0F, 0F, 18, 100, 1);
		Ceiling.setRotationPoint(-8F, -6F, 10F);
		Ceiling.setTextureSize(256, 256);
		Ceiling.mirror = true;
		setRotation(Ceiling, 1.570796F, 0F, 0F);
		Right1 = new ModelRendererTurbo(this, 59, 0);
		Right1.addBox(0F, 0F, 0F, 2, 110, 6);
		Right1.setRotationPoint(-10F, 18.3F, 0F);
		Right1.setTextureSize(256, 256);
		Right1.mirror = true;
		setRotation(Right1, 1.570796F, 0F, -0.2230717F);
		Right2 = new ModelRendererTurbo(this, 76, 0);
		Right2.addBox(0F, 0F, 0F, 2, 100, 15);
		Right2.setRotationPoint(-11.2F, 13F, 10F);
		Right2.setTextureSize(256, 256);
		Right2.mirror = true;
		setRotation(Right2, 1.570796F, 0F, 0F);
		Right3 = new ModelRendererTurbo(this, 42, 0);
		Right3.addBox(0F, 0F, 0F, 2, 100, 6);
		Right3.setRotationPoint(-11F, -2F, 10F);
		Right3.setTextureSize(256, 256);
		Right3.mirror = true;
		setRotation(Right3, 1.570796F, 0F, 0.3346075F);
		Cab_2 = new ModelRendererTurbo(this, 0, 114);
		Cab_2.addBox(0F, 0F, 0F, 2, 20, 3);
		Cab_2.setRotationPoint(-7.5F, 20.3F, -19F);
		Cab_2.setTextureSize(256, 256);
		Cab_2.mirror = true;
		setRotation(Cab_2, 1.682332F, -0.1115358F, -0.2230717F);
		Cab_3 = new ModelRendererTurbo(this, 0, 138);
		Cab_3.addBox(0F, 0F, 0F, 2, 20, 4);
		Cab_3.setRotationPoint(-8F, 18F, -19F);
		Cab_3.setTextureSize(256, 256);
		Cab_3.mirror = true;
		setRotation(Cab_3, 1.682332F, -0.1115358F, -0.2230717F);
		Cab_4 = new ModelRendererTurbo(this, 11, 114);
		Cab_4.addBox(0F, 0F, 0F, 2, 20, 3);
		Cab_4.setRotationPoint(6.5F, 20F, -19F);
		Cab_4.setTextureSize(256, 256);
		Cab_4.mirror = true;
		setRotation(Cab_4, 1.682332F, 0.1115358F, 0.2230717F);
		Cab_5 = new ModelRendererTurbo(this, 12, 138);
		Cab_5.addBox(0F, 0F, 0F, 2, 20, 3);
		Cab_5.setRotationPoint(7.2F, 17F, -19F);
		Cab_5.setTextureSize(256, 256);
		Cab_5.mirror = true;
		setRotation(Cab_5, 1.682332F, 0.1115358F, 0.2230717F);
		Cab_6 = new ModelRendererTurbo(this, 101, 117);
		Cab_6.addBox(0F, 0F, 0F, 2, 30, 6);
		Cab_6.setRotationPoint(8.4F, 11.3F, -14F);
		Cab_6.setTextureSize(256, 256);
		Cab_6.mirror = true;
		setRotation(Cab_6, 1.979761F, 0.2230717F, -0.2974289F);
		Cab_7 = new ModelRendererTurbo(this, 118, 118);
		Cab_7.addBox(0F, 0F, 0F, 2, 28, 6);
		Cab_7.setRotationPoint(-8.9F, 10.4F, -12F);
		Cab_7.setTextureSize(256, 256);
		Cab_7.mirror = true;
		setRotation(Cab_7, 2.01694F, -0.2230717F, 0.2602503F);
		Cab_8 = new ModelRendererTurbo(this, 12, 198);
		Cab_8.addBox(0F, 0F, 0F, 2, 7, 4);
		Cab_8.setRotationPoint(-5.3F, 19F, -25F);
		Cab_8.setTextureSize(256, 256);
		Cab_8.mirror = true;
		setRotation(Cab_8, 1.384903F, -0.4089647F, -0.2230717F);
		Cab_9 = new ModelRendererTurbo(this, 0, 230);
		Cab_9.addBox(0F, 0F, 0F, 2, 16, 4);
		Cab_9.setRotationPoint(-7.3F, 17.5F, -24F);
		Cab_9.setTextureSize(256, 256);
		Cab_9.mirror = true;
		setRotation(Cab_9, 2.202833F, -0.2602503F, 0.2602503F);
		Cab_10 = new ModelRendererTurbo(this, 240, 234);
		Cab_10.addBox(0F, 0F, 0F, 2, 14, 5);
		Cab_10.setRotationPoint(6F, 18.5F, -23F);
		Cab_10.setTextureSize(256, 256);
		Cab_10.mirror = true;
		setRotation(Cab_10, 2.165654F, 0.4833219F, -0.2974289F);
		Cab_11 = new ModelRendererTurbo(this, 10, 210);
		Cab_11.addBox(0F, 0F, 0F, 2, 7, 5);
		Cab_11.setRotationPoint(4F, 19F, -24F);
		Cab_11.setTextureSize(256, 256);
		Cab_11.mirror = true;
		setRotation(Cab_11, 1.422082F, 0.4461433F, 0.1487144F);
		Cab_12 = new ModelRendererTurbo(this, 23, 238);
		Cab_12.addBox(0F, 0F, 0F, 7, 16, 1);
		Cab_12.setRotationPoint(8F, 6F, -15F);
		Cab_12.setTextureSize(256, 256);
		Cab_12.mirror = true;
		setRotation(Cab_12, 0F, 0F, 1.570796F);
		Cab_13 = new ModelRendererTurbo(this, 46, 240);
		Cab_13.addBox(0F, 0F, 0F, 6, 13, 1);
		Cab_13.setRotationPoint(7F, 13F, -22F);
		Cab_13.setTextureSize(256, 256);
		Cab_13.mirror = true;
		setRotation(Cab_13, 0F, 0F, 1.570796F);
		Cab_14 = new ModelRendererTurbo(this, 68, 222);
		Cab_14.addBox(0F, 0F, 0F, 12, 14, 1);
		Cab_14.setRotationPoint(-6F, 5.4F, -15F);
		Cab_14.setTextureSize(256, 256);
		Cab_14.mirror = true;
		setRotation(Cab_14, -0.9294653F, 0F, 0F);
		Cab_15 = new ModelRendererTurbo(this, 75, 192);
		Cab_15.addBox(0F, 0F, 0F, 2, 28, 1);
		Cab_15.setRotationPoint(8F, -7F, 10F);
		Cab_15.setTextureSize(256, 256);
		Cab_15.mirror = true;
		setRotation(Cab_15, -1.115358F, 0.1487144F, -0.0371786F);
		Cab_16 = new ModelRendererTurbo(this, 68, 192);
		Cab_16.addBox(0F, 0F, 0F, 2, 27, 1);
		Cab_16.setRotationPoint(-8F, -7F, 10F);
		Cab_16.setTextureSize(256, 256);
		Cab_16.mirror = true;
		setRotation(Cab_16, -1.115358F, -0.0743572F, 0F);
		Cab_17 = new ModelRendererTurbo(this, 61, 240);
		Cab_17.addBox(0F, 0F, 0F, 14, 14, 1);
		Cab_17.setRotationPoint(-6F, -6.95F, 10F);
		Cab_17.setTextureSize(256, 256);
		Cab_17.mirror = true;
		setRotation(Cab_17, -1.115358F, 0F, 0F);
		Cab_18 = new ModelRendererTurbo(this, 92, 240);
		Cab_18.addBox(0F, 0F, 0F, 13, 15, 1);
		Cab_18.setRotationPoint(-6F, -1F, -2F);
		Cab_18.setTextureSize(256, 256);
		Cab_18.mirror = true;
		setRotation(Cab_18, -1.115358F, 0F, 0F);
		Cab_19 = new ModelRendererTurbo(this, 95, 214);
		Cab_19.addBox(0F, 0F, 0F, 2, 15, 5);
		Cab_19.setRotationPoint(8.4F, 14F, -14F);
		Cab_19.setTextureSize(256, 256);
		Cab_19.mirror = true;
		setRotation(Cab_19, 1.663743F, 0.1487144F, -0.0371786F);
		Cab_20 = new ModelRendererTurbo(this, 89, 170);
		Cab_20.addBox(0F, 0F, 0F, 2, 10, 5);
		Cab_20.setRotationPoint(10.5F, 12.6F, 0F);
		Cab_20.setTextureSize(256, 256);
		Cab_20.mirror = true;
		setRotation(Cab_20, 1.570796F, -0.0371786F, -0.0371786F);
		Cab_21 = new ModelRendererTurbo(this, 79, 138);
		Cab_21.addBox(0F, 0F, 0F, 1, 12, 3);
		Cab_21.setRotationPoint(7.9F, 7F, -14F);
		Cab_21.setTextureSize(256, 256);
		Cab_21.mirror = true;
		setRotation(Cab_21, -0.8272241F, 0.3717861F, -0.1858931F);
		Cab_22 = new ModelRendererTurbo(this, 83, 200);
		Cab_22.addBox(0F, 0F, 0F, 1, 4, 5);
		Cab_22.setRotationPoint(7.4F, 10F, -18F);
		Cab_22.setTextureSize(256, 256);
		Cab_22.mirror = true;
		setRotation(Cab_22, 0.0371786F, 0.3717861F, -0.0371786F);
		Cab_23 = new ModelRendererTurbo(this, 96, 192);
		Cab_23.addBox(0F, 0F, 0F, 1, 10, 6);
		Cab_23.setRotationPoint(-11F, 13F, 0F);
		Cab_23.setTextureSize(256, 256);
		Cab_23.mirror = true;
		setRotation(Cab_23, 1.570796F, 0F, -0.0371786F);
		Cab_24 = new ModelRendererTurbo(this, 110, 204);
		Cab_24.addBox(0F, 0F, 0F, 1, 16, 5);
		Cab_24.setRotationPoint(-8.7F, 14F, -15F);
		Cab_24.setTextureSize(256, 256);
		Cab_24.mirror = true;
		setRotation(Cab_24, 1.663743F, -0.1487144F, -0.0371786F);
		Cab_25 = new ModelRendererTurbo(this, 88, 138);
		Cab_25.addBox(0F, 0F, 0F, 1, 12, 4);
		Cab_25.setRotationPoint(-8.6F, 7F, -13F);
		Cab_25.setTextureSize(256, 256);
		Cab_25.mirror = true;
		setRotation(Cab_25, -0.9015813F, -0.2974289F, 0.1487144F);
		Cab_26 = new ModelRendererTurbo(this, 0, 163);
		Cab_26.addBox(0F, 0F, 0F, 1, 27, 4);
		Cab_26.setRotationPoint(9.5F, 12F, -13F);
		Cab_26.setTextureSize(256, 256);
		Cab_26.mirror = true;
		setRotation(Cab_26, 1.979761F, 0.1115358F, -0.1115358F);
		Cab_27 = new ModelRendererTurbo(this, 104, 154);
		Cab_27.addBox(0F, 0F, 0F, 1, 23, 4);
		Cab_27.setRotationPoint(-8F, 9F, -12F);
		Cab_27.setTextureSize(256, 256);
		Cab_27.mirror = true;
		setRotation(Cab_27, 1.663743F, -0.1487144F, 0.0371786F);
		Cab_28 = new ModelRendererTurbo(this, 115, 154);
		Cab_28.addBox(0F, 0F, 0F, 1, 14, 4);
		Cab_28.setRotationPoint(10F, 8F, -4F);
		Cab_28.setTextureSize(256, 256);
		Cab_28.mirror = true;
		setRotation(Cab_28, 1.552207F, 0.0743572F, -0.0371786F);
		Cab_29 = new ModelRendererTurbo(this, 112, 188);
		Cab_29.addBox(0F, 0F, 0F, 1, 12, 2);
		Cab_29.setRotationPoint(-9F, 4F, -1F);
		Cab_29.setTextureSize(256, 256);
		Cab_29.mirror = true;
		setRotation(Cab_29, 1.645154F, -0.1858931F, -0.0371786F);
		Cab_30 = new ModelRendererTurbo(this, 115, 175);
		Cab_30.addBox(0F, 0F, 0F, 1, 7, 3);
		Cab_30.setRotationPoint(-10F, 2F, 4F);
		Cab_30.setTextureSize(256, 256);
		Cab_30.mirror = true;
		setRotation(Cab_30, 1.645154F, -0.1858931F, -0.0371786F);
		Cab_31 = new ModelRendererTurbo(this, 124, 175);
		Cab_31.addBox(0F, 0F, 0F, 1, 6, 3);
		Cab_31.setRotationPoint(10.4F, 5F, 5F);
		Cab_31.setTextureSize(256, 256);
		Cab_31.mirror = true;
		setRotation(Cab_31, 1.645154F, 0.1115358F, 0F);
		Coupler_1 = new ModelRendererTurbo(this, 12, 164);
		Coupler_1.addBox(0F, 0F, 0F, 4, 8, 2);
		Coupler_1.setRotationPoint(4F, 15F, -24F);
		Coupler_1.setTextureSize(256, 256);
		Coupler_1.mirror = true;
		setRotation(Coupler_1, 0F, 0F, 1.570796F);
		Coupler_2 = new ModelRendererTurbo(this, 12, 175);
		Coupler_2.addBox(0F, 0F, 0F, 2, 2, 2);
		Coupler_2.setRotationPoint(-3F, 16F, -26F);
		Coupler_2.setTextureSize(256, 256);
		Coupler_2.mirror = true;
		setRotation(Coupler_2, 0F, 0F, 0F);
		ControlPanel1 = new ModelRendererTurbo(this, 68, 182);
		ControlPanel1.addBox(0F, 0F, 0F, 5, 5, 4);
		ControlPanel1.setRotationPoint(4F, 8F, -14F);
		ControlPanel1.setTextureSize(256, 256);
		ControlPanel1.mirror = true;
		setRotation(ControlPanel1, 0F, -0.4363323F, 0F);
		ControlPanel2 = new ModelRendererTurbo(this, 69, 157);
		ControlPanel2.addBox(0F, 0F, 0F, 5, 5, 4);
		ControlPanel2.setRotationPoint(-8F, 8F, -12F);
		ControlPanel2.setTextureSize(256, 256);
		ControlPanel2.mirror = true;
		setRotation(ControlPanel2, 0F, 0.4363323F, 0F);
		ControlPanel3 = new ModelRendererTurbo(this, 69, 168);
		ControlPanel3.addBox(0F, 0F, 0F, 5, 8, 4);
		ControlPanel3.setRotationPoint(4F, 8F, -14F);
		ControlPanel3.setTextureSize(256, 256);
		ControlPanel3.mirror = true;
		setRotation(ControlPanel3, 0F, 0F, 1.570796F);
		ControlPanel4 = new ModelRendererTurbo(this, 69, 146);
		ControlPanel4.addBox(0F, 0F, 0F, 3, 8, 1);
		ControlPanel4.setRotationPoint(4F, 5F, -14F);
		ControlPanel4.setTextureSize(256, 256);
		ControlPanel4.mirror = true;
		setRotation(ControlPanel4, 0F, 0F, 1.570796F);
		ControlPanel5 = new ModelRendererTurbo(this, 68, 138);
		ControlPanel5.addBox(0F, 0F, 0F, 3, 5, 1);
		ControlPanel5.setRotationPoint(4F, 8F, -14F);
		ControlPanel5.setTextureSize(256, 256);
		ControlPanel5.mirror = true;
		setRotation(ControlPanel5, 0.4363323F, 0F, -1.570796F);
		ControlPanel6 = new ModelRendererTurbo(this, 68, 131);
		ControlPanel6.addBox(0F, 0F, 0F, 3, 5, 1);
		ControlPanel6.setRotationPoint(-8F, 8F, -12F);
		ControlPanel6.setTextureSize(256, 256);
		ControlPanel6.mirror = true;
		setRotation(ControlPanel6, -0.4363323F, 0F, -1.570796F);
		WallToCab = new ModelRendererTurbo(this, 196, 234);
		WallToCab.addBox(0F, 0F, 0F, 20, 20, 1);
		WallToCab.setRotationPoint(-10F, -6F, 10F);
		WallToCab.setTextureSize(256, 256);
		WallToCab.mirror = true;
		setRotation(WallToCab, 0F, 0F, 0F);
		MiddleWall1 = new ModelRendererTurbo(this, 196, 212);
		MiddleWall1.addBox(0F, 0F, 0F, 20, 20, 1);
		MiddleWall1.setRotationPoint(-10F, -6F, 68F);
		MiddleWall1.setTextureSize(256, 256);
		MiddleWall1.mirror = true;
		setRotation(MiddleWall1, 0F, 0F, 0F);
		MiddleWall2 = new ModelRendererTurbo(this, 196, 168);
		MiddleWall2.addBox(0F, 0F, 0F, 20, 20, 1);
		MiddleWall2.setRotationPoint(-9F, -6F, 48F);
		MiddleWall2.setTextureSize(256, 256);
		MiddleWall2.mirror = true;
		setRotation(MiddleWall2, 0F, 0F, 0F);
		BackWall = new ModelRendererTurbo(this, 196, 190);
		BackWall.addBox(0F, 0F, 0F, 21, 20, 1);
		BackWall.setRotationPoint(-10F, -6F, 108F);
		BackWall.setTextureSize(256, 256);
		BackWall.mirror = true;
		setRotation(BackWall, 0F, 0F, 0F);
		DriverChair1 = new ModelRendererTurbo(this, 82, 212);
		DriverChair1.addBox(0F, 0F, 0F, 3, 4, 1);
		DriverChair1.setRotationPoint(2F, 11F, -4F);
		DriverChair1.setTextureSize(256, 256);
		DriverChair1.mirror = true;
		setRotation(DriverChair1, 0F, 0F, 1.570796F);
		DriverChair2 = new ModelRendererTurbo(this, 69, 119);
		DriverChair2.addBox(0F, 0F, 0F, 1, 6, 4);
		DriverChair2.setRotationPoint(3F, 10F, -6F);
		DriverChair2.setTextureSize(256, 256);
		DriverChair2.mirror = true;
		setRotation(DriverChair2, 0F, 0F, 1.570796F);
		DriverChair3 = new ModelRendererTurbo(this, 90, 158);
		DriverChair3.addBox(0F, 0F, 0F, 6, 5, 1);
		DriverChair3.setRotationPoint(-3F, 5F, -3F);
		DriverChair3.setTextureSize(256, 256);
		DriverChair3.mirror = true;
		setRotation(DriverChair3, 0F, 0F, 0F);
		Wheels1 = new ModelRendererTurbo(this, 124, 186);
		Wheels1.addBox(0F, 0F, 0F, 6, 21, 16);
		Wheels1.setRotationPoint(11F, 18F, 2F);
		Wheels1.setTextureSize(256, 256);
		Wheels1.mirror = true;
		setRotation(Wheels1, 0F, 0F, 1.570796F);
		Wheels2 = new ModelRendererTurbo(this, 134, 151);
		Wheels2.addBox(0F, 0F, 0F, 6, 21, 12);
		Wheels2.setRotationPoint(11F, 18F, 102F);
		Wheels2.setTextureSize(256, 256);
		Wheels2.mirror = true;
		setRotation(Wheels2, 0F, 0F, 1.570796F);
		Seats1 = new ModelRendererTurbo(this, 136, 148);
		Seats1.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats1.setRotationPoint(-9F, 12F, 11F);
		Seats1.setTextureSize(256, 256);
		Seats1.mirror = true;
		setRotation(Seats1, 0F, 0F, 0F);
		Seats2 = new ModelRendererTurbo(this, 136, 148);
		Seats2.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats2.setRotationPoint(3F, 12F, 11F);
		Seats2.setTextureSize(256, 256);
		Seats2.mirror = true;
		setRotation(Seats2, 0F, 0F, 0F);
		Seats3 = new ModelRendererTurbo(this, 136, 148);
		Seats3.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats3.setRotationPoint(-9F, 12F, 20F);
		Seats3.setTextureSize(256, 256);
		Seats3.mirror = true;
		setRotation(Seats3, 0F, 0F, 0F);
		Seats4 = new ModelRendererTurbo(this, 136, 148);
		Seats4.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats4.setRotationPoint(3F, 12F, 20F);
		Seats4.setTextureSize(256, 256);
		Seats4.mirror = true;
		setRotation(Seats4, 0F, 0F, 0F);
		Seats5 = new ModelRendererTurbo(this, 136, 148);
		Seats5.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats5.setRotationPoint(-9F, 12F, 30F);
		Seats5.setTextureSize(256, 256);
		Seats5.mirror = true;
		setRotation(Seats5, 0F, 0F, 0F);
		Seats6 = new ModelRendererTurbo(this, 136, 148);
		Seats6.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats6.setRotationPoint(3F, 12F, 30F);
		Seats6.setTextureSize(256, 256);
		Seats6.mirror = true;
		setRotation(Seats6, 0F, 0F, 0F);
		Seats7 = new ModelRendererTurbo(this, 136, 148);
		Seats7.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats7.setRotationPoint(-9F, 12F, 40F);
		Seats7.setTextureSize(256, 256);
		Seats7.mirror = true;
		setRotation(Seats7, 0F, 0F, 0F);
		Seats8 = new ModelRendererTurbo(this, 136, 148);
		Seats8.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats8.setRotationPoint(3F, 12F, 40F);
		Seats8.setTextureSize(256, 256);
		Seats8.mirror = true;
		setRotation(Seats8, 0F, 0F, 0F);
		Seats9 = new ModelRendererTurbo(this, 136, 148);
		Seats9.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats9.setRotationPoint(3F, 12F, 70F);
		Seats9.setTextureSize(256, 256);
		Seats9.mirror = true;
		setRotation(Seats9, 0F, 0F, 0F);
		Seats10 = new ModelRendererTurbo(this, 136, 148);
		Seats10.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats10.setRotationPoint(-9F, 12F, 70F);
		Seats10.setTextureSize(256, 256);
		Seats10.mirror = true;
		setRotation(Seats10, 0F, 0F, 0F);
		Seats11 = new ModelRendererTurbo(this, 136, 148);
		Seats11.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats11.setRotationPoint(-9F, 12F, 80F);
		Seats11.setTextureSize(256, 256);
		Seats11.mirror = true;
		setRotation(Seats11, 0F, 0F, 0F);
		Seats12 = new ModelRendererTurbo(this, 136, 148);
		Seats12.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats12.setRotationPoint(3F, 12F, 80F);
		Seats12.setTextureSize(256, 256);
		Seats12.mirror = true;
		setRotation(Seats12, 0F, 0F, 0F);
		Seats13 = new ModelRendererTurbo(this, 136, 148);
		Seats13.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats13.setRotationPoint(3F, 12F, 90F);
		Seats13.setTextureSize(256, 256);
		Seats13.mirror = true;
		setRotation(Seats13, 0F, 0F, 0F);
		Seats14 = new ModelRendererTurbo(this, 136, 148);
		Seats14.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats14.setRotationPoint(-9F, 12F, 90F);
		Seats14.setTextureSize(256, 256);
		Seats14.mirror = true;
		setRotation(Seats14, 0F, 0F, 0F);
		Seats15 = new ModelRendererTurbo(this, 136, 148);
		Seats15.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats15.setRotationPoint(3F, 12F, 100F);
		Seats15.setTextureSize(256, 256);
		Seats15.mirror = true;
		setRotation(Seats15, 0F, 0F, 0F);
		Seats16 = new ModelRendererTurbo(this, 136, 148);
		Seats16.addBox(0F, 0F, 0F, 7, 2, 1);
		Seats16.setRotationPoint(-9F, 12F, 100F);
		Seats16.setTextureSize(256, 256);
		Seats16.mirror = true;
		setRotation(Seats16, 0F, 0F, 0F);
		Seats17 = new ModelRendererTurbo(this, 136, 134);
		Seats17.addBox(0F, 0F, 0F, 1, 7, 3);
		Seats17.setRotationPoint(10F, 11F, 11F);
		Seats17.setTextureSize(256, 256);
		Seats17.mirror = true;
		setRotation(Seats17, 0F, 0F, 1.570796F);
		Seats18 = new ModelRendererTurbo(this, 136, 134);
		Seats18.addBox(0F, 0F, 0F, 1, 7, 3);
		Seats18.setRotationPoint(-2F, 11F, 11F);
		Seats18.setTextureSize(256, 256);
		Seats18.mirror = true;
		setRotation(Seats18, 0F, 0F, 1.570796F);
		Seats19 = new ModelRendererTurbo(this, 136, 134);
		Seats19.addBox(0F, 0F, 0F, 1, 7, 3);
		Seats19.setRotationPoint(-2F, 11F, 70F);
		Seats19.setTextureSize(256, 256);
		Seats19.mirror = true;
		setRotation(Seats19, 0F, 0F, 1.570796F);
		Seats20 = new ModelRendererTurbo(this, 136, 134);
		Seats20.addBox(0F, 0F, 0F, 1, 7, 3);
		Seats20.setRotationPoint(10F, 11F, 70F);
		Seats20.setTextureSize(256, 256);
		Seats20.mirror = true;
		setRotation(Seats20, 0F, 0F, 1.570796F);
		Seats21 = new ModelRendererTurbo(this, 136, 120);
		Seats21.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats21.setRotationPoint(-2F, 11F, 18F);
		Seats21.setTextureSize(256, 256);
		Seats21.mirror = true;
		setRotation(Seats21, 0F, 0F, 1.570796F);
		Seats22 = new ModelRendererTurbo(this, 136, 120);
		Seats22.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats22.setRotationPoint(10F, 11F, 18F);
		Seats22.setTextureSize(256, 256);
		Seats22.mirror = true;
		setRotation(Seats22, 0F, 0F, 1.570796F);
		Seats23 = new ModelRendererTurbo(this, 136, 120);
		Seats23.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats23.setRotationPoint(10F, 11F, 28F);
		Seats23.setTextureSize(256, 256);
		Seats23.mirror = true;
		setRotation(Seats23, 0F, 0F, 1.570796F);
		Seats24 = new ModelRendererTurbo(this, 136, 120);
		Seats24.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats24.setRotationPoint(-2F, 11F, 28F);
		Seats24.setTextureSize(256, 256);
		Seats24.mirror = true;
		setRotation(Seats24, 0F, 0F, 1.570796F);
		Seats25 = new ModelRendererTurbo(this, 136, 120);
		Seats25.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats25.setRotationPoint(-2F, 11F, 38F);
		Seats25.setTextureSize(256, 256);
		Seats25.mirror = true;
		setRotation(Seats25, 0F, 0F, 1.570796F);
		Seats26 = new ModelRendererTurbo(this, 136, 120);
		Seats26.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats26.setRotationPoint(10F, 11F, 78F);
		Seats26.setTextureSize(256, 256);
		Seats26.mirror = true;
		setRotation(Seats26, 0F, 0F, 1.570796F);
		Seats27 = new ModelRendererTurbo(this, 136, 120);
		Seats27.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats27.setRotationPoint(-2F, 11F, 78F);
		Seats27.setTextureSize(256, 256);
		Seats27.mirror = true;
		setRotation(Seats27, 0F, 0F, 1.570796F);
		Seats28 = new ModelRendererTurbo(this, 136, 120);
		Seats28.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats28.setRotationPoint(-2F, 11F, 88F);
		Seats28.setTextureSize(256, 256);
		Seats28.mirror = true;
		setRotation(Seats28, 0F, 0F, 1.570796F);
		Seats29 = new ModelRendererTurbo(this, 138, 120);
		Seats29.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats29.setRotationPoint(10F, 11F, 88F);
		Seats29.setTextureSize(256, 256);
		Seats29.mirror = true;
		setRotation(Seats29, 0F, 0F, 1.570796F);
		Seats30 = new ModelRendererTurbo(this, 136, 120);
		Seats30.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats30.setRotationPoint(-2F, 11F, 98F);
		Seats30.setTextureSize(256, 256);
		Seats30.mirror = true;
		setRotation(Seats30, 0F, 0F, 1.570796F);
		Seats31 = new ModelRendererTurbo(this, 136, 120);
		Seats31.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats31.setRotationPoint(10F, 11F, 98F);
		Seats31.setTextureSize(256, 256);
		Seats31.mirror = true;
		setRotation(Seats31, 0F, 0F, 1.570796F);
		Seats32 = new ModelRendererTurbo(this, 178, 223);
		Seats32.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats32.setRotationPoint(3F, 6F, 11F);
		Seats32.setTextureSize(256, 256);
		Seats32.mirror = true;
		setRotation(Seats32, 0F, 0F, 0F);
		Seats33 = new ModelRendererTurbo(this, 178, 223);
		Seats33.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats33.setRotationPoint(-9F, 6F, 11F);
		Seats33.setTextureSize(256, 256);
		Seats33.mirror = true;
		setRotation(Seats33, 0F, 0F, 0F);
		Seats34 = new ModelRendererTurbo(this, 178, 216);
		Seats34.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats34.setRotationPoint(-9F, 6F, 20F);
		Seats34.setTextureSize(256, 256);
		Seats34.mirror = true;
		setRotation(Seats34, 0F, 0F, 0F);
		Seats35 = new ModelRendererTurbo(this, 178, 216);
		Seats35.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats35.setRotationPoint(3F, 6F, 20F);
		Seats35.setTextureSize(256, 256);
		Seats35.mirror = true;
		setRotation(Seats35, 0F, 0F, 0F);
		Seats36 = new ModelRendererTurbo(this, 178, 216);
		Seats36.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats36.setRotationPoint(-9F, 6F, 30F);
		Seats36.setTextureSize(256, 256);
		Seats36.mirror = true;
		setRotation(Seats36, 0F, 0F, 0F);
		Seats37 = new ModelRendererTurbo(this, 178, 216);
		Seats37.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats37.setRotationPoint(3F, 6F, 30F);
		Seats37.setTextureSize(256, 256);
		Seats37.mirror = true;
		setRotation(Seats37, 0F, 0F, 0F);
		Seats38 = new ModelRendererTurbo(this, 136, 120);
		Seats38.addBox(0F, 0F, 0F, 1, 7, 5);
		Seats38.setRotationPoint(10F, 11F, 38F);
		Seats38.setTextureSize(256, 256);
		Seats38.mirror = true;
		setRotation(Seats38, 0F, 0F, 1.570796F);
		Seats39 = new ModelRendererTurbo(this, 178, 216);
		Seats39.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats39.setRotationPoint(-9F, 6F, 40F);
		Seats39.setTextureSize(256, 256);
		Seats39.mirror = true;
		setRotation(Seats39, 0F, 0F, 0F);
		Seats40 = new ModelRendererTurbo(this, 178, 216);
		Seats40.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats40.setRotationPoint(3F, 6F, 40F);
		Seats40.setTextureSize(256, 256);
		Seats40.mirror = true;
		setRotation(Seats40, 0F, 0F, 0F);
		Seats41 = new ModelRendererTurbo(this, 178, 223);
		Seats41.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats41.setRotationPoint(-9F, 6F, 70F);
		Seats41.setTextureSize(256, 256);
		Seats41.mirror = true;
		setRotation(Seats41, 0F, 0F, 0F);
		Seats42 = new ModelRendererTurbo(this, 178, 223);
		Seats42.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats42.setRotationPoint(3F, 6F, 70F);
		Seats42.setTextureSize(256, 256);
		Seats42.mirror = true;
		setRotation(Seats42, 0F, 0F, 0F);
		Seats43 = new ModelRendererTurbo(this, 178, 216);
		Seats43.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats43.setRotationPoint(-9F, 6F, 80F);
		Seats43.setTextureSize(256, 256);
		Seats43.mirror = true;
		setRotation(Seats43, 0F, 0F, 0F);
		Seats44 = new ModelRendererTurbo(this, 178, 216);
		Seats44.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats44.setRotationPoint(3F, 6F, 80F);
		Seats44.setTextureSize(256, 256);
		Seats44.mirror = true;
		setRotation(Seats44, 0F, 0F, 0F);
		Seats45 = new ModelRendererTurbo(this, 178, 216);
		Seats45.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats45.setRotationPoint(-9F, 6F, 90F);
		Seats45.setTextureSize(256, 256);
		Seats45.mirror = true;
		setRotation(Seats45, 0F, 0F, 0F);
		Seats46 = new ModelRendererTurbo(this, 178, 216);
		Seats46.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats46.setRotationPoint(3F, 6F, 90F);
		Seats46.setTextureSize(256, 256);
		Seats46.mirror = true;
		setRotation(Seats46, 0F, 0F, 0F);
		Seats47 = new ModelRendererTurbo(this, 178, 216);
		Seats47.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats47.setRotationPoint(-9F, 6F, 100F);
		Seats47.setTextureSize(256, 256);
		Seats47.mirror = true;
		setRotation(Seats47, 0F, 0F, 0F);
		Seats48 = new ModelRendererTurbo(this, 178, 216);
		Seats48.addBox(0F, 0F, 0F, 7, 5, 1);
		Seats48.setRotationPoint(3F, 6F, 100F);
		Seats48.setTextureSize(256, 256);
		Seats48.mirror = true;
		setRotation(Seats48, 0F, 0F, 0F);
		Right4 = new ModelRendererTurbo(this, 172, 128);
		Right4.addBox(0F, 0F, 0F, 1, 82, 4);
		Right4.setRotationPoint(-9F, 22F, 19F);
		Right4.setTextureSize(256, 256);
		Right4.mirror = true;
		setRotation(Right4, 1.570796F, 0F, -0.2974289F);
		Left4 = new ModelRendererTurbo(this, 246, 146);
		Left4.addBox(0F, 0F, 0F, 1, 82, 4);
		Left4.setRotationPoint(9F, 22F, 19F);
		Left4.setTextureSize(256, 256);
		Left4.mirror = true;
		setRotation(Left4, 1.570796F, 0F, 0.2974289F);
		Cab_32 = new ModelRendererTurbo(this, 150, 118);
		Cab_32.addBox(0F, 0F, 0F, 2, 20, 4);
		Cab_32.setRotationPoint(5.9F, 23.8F, -18F);
		Cab_32.setTextureSize(256, 256);
		Cab_32.mirror = true;
		setRotation(Cab_32, 1.682332F, 0.1115358F, 0.2230717F);
		Cab_33 = new ModelRendererTurbo(this, 150, 118);
		Cab_33.addBox(0F, 0F, 0F, 2, 20, 4);
		Cab_33.setRotationPoint(-7F, 23.8F, -18F);
		Cab_33.setTextureSize(256, 256);
		Cab_33.mirror = true;
		setRotation(Cab_33, 1.682332F, -0.1115358F, -0.2230717F);
		Bottom = new ModelRendererTurbo(this, 220, 0);
		Bottom.addBox(0F, 0F, 0F, 17, 82, 1);
		Bottom.setRotationPoint(-8F, 22F, 19F);
		Bottom.setTextureSize(256, 256);
		Bottom.mirror = true;
		setRotation(Bottom, 1.570796F, 0F, 0F);
		Showel5 = new ModelRendererTurbo(this, 183, 146);
		Showel5.addBox(0F, 0F, 0F, 7, 2, 1);
		Showel5.setRotationPoint(-7F, 20.8F, -20.4F);
		Showel5.setTextureSize(256, 256);
		Showel5.mirror = true;
		setRotation(Showel5, 0.6320364F, 0.2230717F, 0.0371786F);
		Showel6 = new ModelRendererTurbo(this, 183, 146);
		Showel6.addBox(0F, 0F, 0F, 8, 2, 1);
		Showel6.setRotationPoint(0F, 21F, -21.9F);
		Showel6.setTextureSize(256, 256);
		Showel6.mirror = true;
		setRotation(Showel6, 0.669215F, -0.3717861F, 0F);
		Showel7 = new ModelRendererTurbo(this, 183, 146);
		Showel7.addBox(0F, 0F, 0F, 8, 2, 1);
		Showel7.setRotationPoint(0F, 22F, -20.8F);
		Showel7.setTextureSize(256, 256);
		Showel7.mirror = true;
		setRotation(Showel7, -0.1858931F, -0.4461433F, 0F);
		Showel8 = new ModelRendererTurbo(this, 183, 146);
		Showel8.addBox(0F, 0F, 0F, 7, 2, 1);
		Showel8.setRotationPoint(-7F, 22F, -19.2F);
		Showel8.setTextureSize(256, 256);
		Showel8.mirror = true;
		setRotation(Showel8, -0.2230717F, 0.2230717F, 0F);
		Showel9 = new ModelRendererTurbo(this, 205, 118);
		Showel9.addBox(0F, 0F, 0F, 12, 1, 12);
		Showel9.setRotationPoint(-6F, 18.5F, -24F);
		Showel9.setTextureSize(256, 256);
		Showel9.mirror = true;
		setRotation(Showel9, -0.2602503F, 0F, 0F);
		Shape1 = new ModelRendererTurbo(this, 164, 102);
		Shape1.addBox(0F, 0F, 0F, 17, 15, 1);
		Shape1.setRotationPoint(-8F, 22.5F, -13F);
		Shape1.setTextureSize(256, 256);
		Shape1.mirror = true;
		setRotation(Shape1, 1.645154F, 0F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		GL11.glPushMatrix();
		GL11.glRotatef(90,0,1,0);
		GL11.glRotatef(180,0,0,1);
		GL11.glTranslated(0,-1.4f,-6);
		GL11.glScaled(0.7,0.9,1);
		super.render(entity, f, f1, f2, f3, f4, f5);
		//setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		Left1.render(f5);
		Left2.render(f5);
		Left3.render(f5);
		Floor1.render(f5);
		Floor2.render(f5);
		Floor_3.render(f5);
		Floor_4.render(f5);
		Floor_5.render(f5);
		Ceiling.render(f5);
		Right1.render(f5);
		Right2.render(f5);
		Right3.render(f5);
		Cab_2.render(f5);
		Cab_3.render(f5);
		Cab_4.render(f5);
		Cab_5.render(f5);
		Cab_6.render(f5);
		Cab_7.render(f5);
		Cab_8.render(f5);
		Cab_9.render(f5);
		Cab_10.render(f5);
		Cab_11.render(f5);
		Cab_12.render(f5);
		Cab_13.render(f5);
		Cab_14.render(f5);
		Cab_15.render(f5);
		Cab_16.render(f5);
		Cab_17.render(f5);
		Cab_18.render(f5);
		Cab_19.render(f5);
		Cab_20.render(f5);
		Cab_21.render(f5);
		Cab_22.render(f5);
		Cab_23.render(f5);
		Cab_24.render(f5);
		Cab_25.render(f5);
		Cab_26.render(f5);
		Cab_27.render(f5);
		Cab_28.render(f5);
		Cab_29.render(f5);
		Cab_30.render(f5);
		Cab_31.render(f5);
		Coupler_1.render(f5);
		Coupler_2.render(f5);
		ControlPanel1.render(f5);
		ControlPanel2.render(f5);
		ControlPanel3.render(f5);
		ControlPanel4.render(f5);
		ControlPanel5.render(f5);
		ControlPanel6.render(f5);
		WallToCab.render(f5);
		MiddleWall1.render(f5);
		MiddleWall2.render(f5);
		BackWall.render(f5);
		DriverChair1.render(f5);
		DriverChair2.render(f5);
		DriverChair3.render(f5);
		Wheels1.render(f5);
		Wheels2.render(f5);
		Seats1.render(f5);
		Seats2.render(f5);
		Seats3.render(f5);
		Seats4.render(f5);
		Seats5.render(f5);
		Seats6.render(f5);
		Seats7.render(f5);
		Seats8.render(f5);
		Seats9.render(f5);
		Seats10.render(f5);
		Seats11.render(f5);
		Seats12.render(f5);
		Seats13.render(f5);
		Seats14.render(f5);
		Seats15.render(f5);
		Seats16.render(f5);
		Seats17.render(f5);
		Seats18.render(f5);
		Seats19.render(f5);
		Seats20.render(f5);
		Seats21.render(f5);
		Seats22.render(f5);
		Seats23.render(f5);
		Seats24.render(f5);
		Seats25.render(f5);
		Seats26.render(f5);
		Seats27.render(f5);
		Seats28.render(f5);
		Seats29.render(f5);
		Seats30.render(f5);
		Seats31.render(f5);
		Seats32.render(f5);
		Seats33.render(f5);
		Seats34.render(f5);
		Seats35.render(f5);
		Seats36.render(f5);
		Seats37.render(f5);
		Seats38.render(f5);
		Seats39.render(f5);
		Seats40.render(f5);
		Seats41.render(f5);
		Seats42.render(f5);
		Seats43.render(f5);
		Seats44.render(f5);
		Seats45.render(f5);
		Seats46.render(f5);
		Seats47.render(f5);
		Seats48.render(f5);
		Right4.render(f5);
		Left4.render(f5);
		Cab_32.render(f5);
		Cab_33.render(f5);
		Bottom.render(f5);
		Showel5.render(f5);
		Showel6.render(f5);
		Showel7.render(f5);
		Showel8.render(f5);
		Showel9.render(f5);
		Shape1.render(f5);
		GL11.glPopMatrix();
	}

	private void setRotation(ModelRendererTurbo model, float x, float y, float z)
	{
		model.rotateAngleX = x* TraincraftUtil.degreesF;
		model.rotateAngleY = y* TraincraftUtil.degreesF;
		model.rotateAngleZ = z* TraincraftUtil.degreesF;
	}

}
