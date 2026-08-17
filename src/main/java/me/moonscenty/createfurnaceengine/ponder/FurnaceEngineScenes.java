package me.moonscenty.createfurnaceengine.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import me.moonscenty.createfurnaceengine.content.FurnaceEngineBlock;
import me.moonscenty.createfurnaceengine.registry.ModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public final class FurnaceEngineScenes {
    private static final BlockPos FURNACE = new BlockPos(3, 1, 3);
    private static final BlockPos ENGINE = new BlockPos(4, 1, 3);
    private static final BlockPos SHAFT = new BlockPos(6, 1, 3);
    private static final BlockPos FRONT = new BlockPos(3, 1, 4);
    private static final BlockPos SECOND_ENGINE = new BlockPos(3, 1, 2);
    private static final BlockPos HEAT_SINK = new BlockPos(2, 1, 3);

    private FurnaceEngineScenes() {}

    public static void placement(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = prepare(builder, util, "furnace_engine_placement",
            "Installing a Furnace Engine");
        Selection furnace = util.select().position(FURNACE);
        Selection engine = util.select().position(ENGINE);

        setMachineBlocks(scene, false, false);
        scene.world().showSection(furnace, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(60)
            .text("Furnace Engines use a running Furnace, Blast Furnace, or Smoker as their heat source")
            .pointAt(util.vector().topOf(FURNACE)).placeNearTarget();
        scene.idle(70);

        scene.overlay().showControls(util.vector().blockSurface(FURNACE, Direction.EAST), Pointing.LEFT, 45)
            .withItem(ModBlocks.FURNACE_ENGINE.asItem().getDefaultInstance()).rightClick();
        scene.idle(10);
        scene.world().showSection(engine, Direction.WEST);
        scene.effects().indicateSuccess(ENGINE);
        scene.idle(25);
        scene.overlay().showText(65).attachKeyFrame()
            .text("Attach the Engine to a side or the top of the furnace")
            .pointAt(util.vector().centerOf(ENGINE)).placeNearTarget();
        scene.idle(75);

        scene.overlay().showOutline(PonderPalette.RED, "front", util.select().position(FRONT), 70);
        scene.overlay().showText(60).colored(PonderPalette.RED)
            .text("The front face is reserved for accessing the furnace and cannot hold an Engine")
            .pointAt(util.vector().centerOf(FRONT)).placeNearTarget();
        scene.idle(75);

        scene.overlay().showOutline(PonderPalette.RED, "second", util.select().position(SECOND_ENGINE), 70);
        scene.overlay().showText(60).attachKeyFrame().colored(PonderPalette.RED)
            .text("Only one Furnace Engine can be attached to each furnace")
            .pointAt(util.vector().centerOf(SECOND_ENGINE)).placeNearTarget();
        scene.idle(75);
        scene.markAsFinished();
    }

    public static void shaftAndDirection(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = prepare(builder, util, "furnace_engine_shaft",
            "Connecting the Kinetic Output");
        showMachine(scene, util, false, false);
        Selection shaft = util.select().position(SHAFT);

        scene.overlay().showText(60)
            .text("The Engine leaves one empty block between its Head and the output Shaft")
            .pointAt(util.vector().centerOf(new BlockPos(5, 1, 3))).placeNearTarget();
        scene.idle(70);

        scene.overlay().showControls(util.vector().centerOf(ENGINE), Pointing.DOWN, 55)
            .withItem(AllBlocks.SHAFT.asStack()).rightClick();
        scene.idle(15);
        scene.world().showSection(shaft, Direction.WEST);
        scene.effects().indicateSuccess(SHAFT);
        scene.idle(20);
        scene.overlay().showText(70).attachKeyFrame().colored(PonderPalette.BLUE)
            .text("Hold a Shaft and right-click the Engine to use its placement guide")
            .pointAt(util.vector().centerOf(SHAFT)).placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(65)
            .text("The Shaft axis must be perpendicular to the direction in which the Engine extends")
            .pointAt(util.vector().centerOf(SHAFT)).placeNearTarget();
        scene.idle(75);

        scene.world().setKineticSpeed(shaft, 24);
        scene.overlay().showControls(util.vector().topOf(ENGINE), Pointing.DOWN, 50)
            .withItem(AllItems.GOGGLES.asStack()).scroll();
        scene.idle(20);
        scene.overlay().showText(70).attachKeyFrame().colored(PonderPalette.BLUE)
            .text("Wear Engineer's Goggles and scroll the value box to choose the rotation direction")
            .pointAt(util.vector().topOf(ENGINE)).placeNearTarget();
        scene.idle(80);
        scene.world().setKineticSpeed(shaft, -24);
        scene.effects().indicateSuccess(ENGINE);
        scene.overlay().showText(55)
            .text("The Shaft and Crank reverse together")
            .pointAt(util.vector().centerOf(SHAFT)).placeNearTarget();
        scene.idle(70);
        scene.markAsFinished();
    }

    public static void operationAndHeatSink(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = prepare(builder, util, "furnace_engine_operation",
            "Power, Warm-up and Heat Sinks");
        showMachine(scene, util, true, false);
        Selection shaft = util.select().position(SHAFT);

        scene.world().setKineticSpeed(shaft, 0);
        scene.overlay().showText(55)
            .text("The Engine remains stopped while the furnace is idle")
            .pointAt(util.vector().centerOf(FURNACE)).placeNearTarget();
        scene.idle(65);

        scene.overlay().showControls(util.vector().blockSurface(FURNACE, Direction.UP), Pointing.DOWN, 45)
            .withItem(new ItemStack(Items.COAL)).rightClick();
        scene.idle(10);
        scene.world().modifyBlock(FURNACE, state -> state.setValue(AbstractFurnaceBlock.LIT, true), false);
        scene.effects().indicateSuccess(FURNACE);
        scene.world().setKineticSpeed(shaft, 1);
        scene.idle(20);
        scene.overlay().showText(70).attachKeyFrame()
            .text("When the furnace starts, the Engine begins at 1 RPM")
            .pointAt(util.vector().centerOf(ENGINE)).placeNearTarget();
        scene.idle(80);

        scene.world().setKineticSpeed(shaft, 8);
        scene.idle(10);
        scene.world().setKineticSpeed(shaft, 16);
        scene.idle(10);
        scene.world().setKineticSpeed(shaft, 24);
        scene.idle(10);
        scene.world().setKineticSpeed(shaft, 32);
        scene.idle(10);
        scene.world().setKineticSpeed(shaft, 40);
        scene.overlay().showText(75).colored(PonderPalette.BLUE)
            .text("It gains 1 RPM every 5 ticks until reaching the configured target RPM")
            .pointAt(util.vector().centerOf(SHAFT)).placeNearTarget();
        scene.idle(85);

        scene.overlay().showText(65)
            .text("Stress capacity scales with the current speed: RPM multiplied by SU per RPM")
            .pointAt(util.vector().centerOf(SHAFT)).placeNearTarget();
        scene.idle(75);

        scene.world().setBlock(HEAT_SINK, Blocks.COPPER_BLOCK.defaultBlockState(), false);
        scene.world().showSection(util.select().position(HEAT_SINK), Direction.EAST);
        scene.effects().indicateSuccess(HEAT_SINK);
        scene.idle(20);
        scene.world().setKineticSpeed(shaft, 32);
        scene.overlay().showText(80).attachKeyFrame().colored(PonderPalette.BLUE)
            .text("An adjacent configured Heat Sink switches the Engine to its Heat Sink RPM and SU/RPM settings")
            .pointAt(util.vector().centerOf(HEAT_SINK)).placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(65)
            .text("By default, any Copper Block touching one of the furnace's six faces is a Heat Sink")
            .pointAt(util.vector().centerOf(HEAT_SINK)).placeNearTarget();
        scene.idle(75);
        scene.markAsFinished();
    }

    private static CreateSceneBuilder prepare(SceneBuilder builder, SceneBuildingUtil util,
        String id, String title) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(.9f);
        scene.rotateCameraY(180);
        scene.showBasePlate();
        scene.idle(10);
        return scene;
    }

    private static void showMachine(CreateSceneBuilder scene, SceneBuildingUtil util,
        boolean lit, boolean heatSink) {
        setMachineBlocks(scene, lit, heatSink);
        scene.world().showSection(util.select().position(FURNACE), Direction.DOWN);
        scene.world().showSection(util.select().position(ENGINE), Direction.DOWN);
        scene.world().showSection(util.select().position(SHAFT), Direction.DOWN);
        if (heatSink)
            scene.world().showSection(util.select().position(HEAT_SINK), Direction.DOWN);
        scene.idle(20);
    }

    private static void setMachineBlocks(CreateSceneBuilder scene, boolean lit, boolean heatSink) {
        BlockState furnace = Blocks.FURNACE.defaultBlockState()
            .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH)
            .setValue(AbstractFurnaceBlock.LIT, lit);
        BlockState engine = ModBlocks.FURNACE_ENGINE.get().defaultBlockState()
            .setValue(FurnaceEngineBlock.FACE, AttachFace.WALL)
            .setValue(FurnaceEngineBlock.FACING, Direction.EAST);
        BlockState shaft = ModBlocks.POWERED_SHAFT.get().defaultBlockState()
            .setValue(ShaftBlock.AXIS, Axis.Z);
        scene.world().setBlock(FURNACE, furnace, false);
        scene.world().setBlock(ENGINE, engine, false);
        scene.world().setBlock(SHAFT, shaft, false);
    }
}
