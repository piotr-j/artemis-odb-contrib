package net.mostlyoriginal.api.utils.builder;

import com.artemis.BaseSystem;
import com.artemis.World;
import net.mostlyoriginal.api.plugin.common.ArtemisPlugin;
import net.mostlyoriginal.api.utils.builder.common.TestEntitySystemA;
import net.mostlyoriginal.api.utils.builder.common.TestEntitySystemB;
import net.mostlyoriginal.api.utils.builder.common.TestEntitySystemC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daan van Yperen
 */
public class WorldConfigurationBuilderSystemTest {

	private WorldConfigurationBuilder builder;

	@Before
	public void setUp() throws Exception {
		builder = new WorldConfigurationBuilder();
	}

	@Test(expected = WorldConfigurationException.class)
	public void should_refuse_duplicate_ystems() {
		builder.with(new TestEntitySystemA(), new TestEntitySystemB(), new TestEntitySystemA()).build();
	}

	@Test
	public void should_create_systems_in_order() {
		BaseSystem system1 = new TestEntitySystemA();
		BaseSystem system2 = new TestEntitySystemB();
		BaseSystem system3 = new TestEntitySystemC();

		World world = new World(new WorldConfigurationBuilder()
				.with(system1, system2)
				.with(system3).build());

		Assert.assertEquals(system1, world.getSystems().get(0));
		Assert.assertEquals(system2, world.getSystems().get(1));
		Assert.assertEquals(system3, world.getSystems().get(2));
	}

	@Test
	public void should_add_systems_as_active_by_default() {
		World world = new World(new WorldConfigurationBuilder()
				.with(new TestEntitySystemA()).build());

		Assert.assertFalse(world.getSystems().get(0).isPassive());
	}

	@Test
	public void should_add_passive_systems_as_passive() {
		World world =  new World(new WorldConfigurationBuilder()
				.withPassive(new TestEntitySystemA()).build());

		Assert.assertTrue(world.getSystems().get(0).isPassive());
	}

	@Test
	public void should_not_carry_over_old_systems_to_new_world() {
		WorldConfigurationBuilder builder = new WorldConfigurationBuilder();
		World world1 = new World(builder.withPassive(new TestEntitySystemA()).build());
		World world2 = new World(builder.build());
		Assert.assertEquals(0, world2.getSystems().size());
	}

	@Test
	public void should_support_multiple_plugins_with_same_system_dependencies() {
		class SharedDependencyPlugin implements ArtemisPlugin {
			@Override
			public void setup(WorldConfigurationBuilder b) {
				builder.dependsOn(TestEntitySystemA.class);
			}
		}
		class SharedDependencyPluginB extends SharedDependencyPlugin {}

		final World world = new World(builder.with(new SharedDependencyPlugin(), new SharedDependencyPluginB()).build());
		Assert.assertNotNull(world.getSystem(TestEntitySystemA.class));
	}

	@Test
	public void should_register_systems_by_priority() {
		BaseSystem system1 = new TestEntitySystemA();
		BaseSystem system2 = new TestEntitySystemB();

		final World world = new World(new WorldConfigurationBuilder()
				.with(WorldConfigurationBuilder.Priority.NORMAL, system1)
				.with(WorldConfigurationBuilder.Priority.HIGHEST, system2).build());

		Assert.assertEquals("Expected system to be loaded by priority.", system1, getLastLoadedSystem(world));
	}

	@Test
	public void should_register_dependency_systems_by_priority() {

		final World world = new World(new WorldConfigurationBuilder()
				.dependsOn(WorldConfigurationBuilder.Priority.NORMAL, TestEntitySystemA.class)
				.dependsOn(WorldConfigurationBuilder.Priority.HIGHEST, TestEntitySystemB.class).build());

		Assert.assertEquals("Expected system to be loaded by priority.", TestEntitySystemA.class, getLastLoadedSystem(world).getClass());
	}

	@Test
	public void should_preserve_system_order_within_same_priority() {

		final World world = new World(new WorldConfigurationBuilder()
				.dependsOn(WorldConfigurationBuilder.Priority.NORMAL, TestEntitySystemA.class, TestEntitySystemC.class)
				.dependsOn(WorldConfigurationBuilder.Priority.HIGHEST, TestEntitySystemB.class).build());

		Assert.assertEquals("Expected system to be loaded by priority.", TestEntitySystemC.class, getLastLoadedSystem(world).getClass());
	}

	private BaseSystem getLastLoadedSystem(World world) {
		return world.getSystems().get(world.getSystems().size()-1);
	}

}
