package net.mostlyoriginal.api.utils.builder;

import com.artemis.BaseSystem;
import com.artemis.Manager;
import com.artemis.SystemInvocationStrategy;
import com.artemis.WorldConfiguration;
import com.artemis.injection.CachedInjector;
import com.artemis.injection.FieldHandler;
import com.artemis.injection.FieldResolver;
import com.artemis.injection.InjectionCache;
import com.artemis.utils.Bag;
import com.artemis.utils.Sort;
import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.ReflectionException;
import net.mostlyoriginal.api.plugin.common.ArtemisPlugin;

/**
 * World builder.
 *
 * @author Daan van Yperen
 */
public class WorldConfigurationBuilder {
	private Bag<Registerable<? extends Manager>> managers;
	private Bag<Registerable<? extends BaseSystem>> systems;
	private Bag<Registerable<? extends FieldResolver>> fieldResolvers;
	private Bag<Registerable<? extends ArtemisPlugin>> plugins;

	private ArtemisPlugin activePlugin;
	private final InjectionCache cache;
	private SystemInvocationStrategy invocationStrategy;

	public WorldConfigurationBuilder() {
		reset();
		cache = new InjectionCache();
	}

	/**
	 * Assemble world with managers and systems.
	 * <p/>
	 * Deprecated: World Configuration
	 */
	public WorldConfiguration build() {
		appendPlugins();
		final WorldConfiguration config = new WorldConfiguration();
		registerManagers(config);
		registerSystems(config);
		registerFieldResolvers(config);
		registerInvocationStrategies(config);
		reset();
		return config;
	}

	private void registerInvocationStrategies(WorldConfiguration config) {
		if ( invocationStrategy != null ) {
			config.setInvocationStrategy(invocationStrategy);
		}
	}

	/**
	 * Append plugin configurations.
	 * Supports plugins registering plugins.
	 */
	private void appendPlugins() {
		int i = 0;

		while (i < plugins.size()) {
			activePlugin = plugins.get(i).item;
			activePlugin.setup(this);
			i++;
		}
		activePlugin = null;
	}

	/**
	 * add custom field handler with resolvers.
	 */
	protected void registerFieldResolvers(WorldConfiguration config) {

		if (fieldResolvers.size() > 0) {
			Sort.instance().sort(fieldResolvers);
			// instance default field handler
			final FieldHandler fieldHandler = new FieldHandler(new InjectionCache());

			for (Registerable<? extends FieldResolver> registerable : fieldResolvers) {
				fieldHandler.addFieldResolver(registerable.item);
			}

			config.setInjector(new CachedInjector().setFieldHandler(fieldHandler));
		}
	}

	/**
	 * add managers to config.
	 */
	private void registerManagers(WorldConfiguration config) {
		Sort.instance().sort(managers);
		for (Registerable<? extends Manager> registerable : managers) {
			config.setManager(registerable.item);
		}
	}

	/**
	 * add systems to config.
	 */
	private void registerSystems(WorldConfiguration config) {
		Sort.instance().sort(systems);
		for (Registerable<? extends BaseSystem> registerable : systems) {
			config.setSystem(registerable.item, registerable.passive);
		}
	}

	/**
	 * Reset builder
	 */
	private void reset() {
		invocationStrategy = null;
		managers = new Bag<>();
		systems = new Bag<>();
		fieldResolvers = new Bag<>();
		plugins = new Bag<>();
	}

	/**
	 * Add field resolver.
	 *
	 * @param fieldResolvers
	 * @return this
	 */
	public WorldConfigurationBuilder register(FieldResolver... fieldResolvers) {
		for (FieldResolver fieldResolver : fieldResolvers) {
			this.fieldResolvers.add(Registerable.of(fieldResolver));
		}
		return this;
	}

	/**
	 * Add system invocation strategy.
	 *
	 * @param strategy strategy to invoke.
	 * @return this
	 */
	public WorldConfigurationBuilder register(SystemInvocationStrategy strategy) {
		this.invocationStrategy = strategy;
		return this;
	}

	/**
	 * Add one or more managers to the world with default priority.
	 * <p/>
	 * Managers track priority separate from system priority, and are always added before systems.
	 *
	 * Only one instance of each class is allowed.
	 * Use {@see #dependsOn} from within plugins whenever possible.
	 *
	 * @param managers Managers to add. Will be added in passed order.
	 * @return this
	 * @throws WorldConfigurationException if registering the same class twice.
	 */
	public WorldConfigurationBuilder with(Manager... managers) {
		return with(Priority.NORMAL, managers);
	}

	/**
	 * Add one or more managers to the world.
	 * <p/>
	 * Managers track priority separate from system priority, and are always added before systems.
	 *
	 * Only one instance of each class is allowed.
	 * Use {@see #dependsOn} from within plugins whenever possible.
	 *
	 * @param priority Priority of managers. Higher priority managers are registered before lower priority managers.
	 * @param managers Managers to add. Will be added in passed order.
	 * @return this
	 * @throws WorldConfigurationException if registering the same class twice.
	 */
	public WorldConfigurationBuilder with(int priority, Manager... managers) {
		for (Manager manager : managers) {

			if (containsType(this.managers, manager.getClass())) {
				throw new WorldConfigurationException("Manager of type " + manager.getClass() + " registered twice. Only once allowed.");
			}

			this.managers.add(Registerable.of(manager, priority));
		}
		return this;
	}

	/**
	 * Specify dependency on managers/systems/plugins.
	 * <p/>
	 * Managers track priority separate from system priority, and are always added before systems.
	 *
	 * @param types required managers and/or systems.
	 * @return this
	 */
	public final WorldConfigurationBuilder dependsOn(Class... types) {
		return dependsOn(Priority.NORMAL, types);
	}

	/**
	 * Specify dependency on managers/systems/plugins.
	 * <p/>
	 * Managers track priority separate from system priority, and are always added before systems.
	 * Plugins do not support priority.
	 *
	 * @param types    required managers and/or systems.
	 * @param priority Higher priority are registered first. Not supported for plugins.
	 * @return this
	 * @throws WorldConfigurationException if unsupported classes are passed or plugins are given a priority.
	 */
	@SuppressWarnings("unchecked")
	public final WorldConfigurationBuilder dependsOn(int priority, Class... types) {
		for (Class type : types) {
			try {
				switch (cache.getFieldClassType(type)) {
					case SYSTEM:
						dependsOnSystem(priority, type);
						break;
					case MANAGER:
						dependsOnManager(priority, type);
						break;
					default:
						if (ClassReflection.isAssignableFrom(ArtemisPlugin.class, type)) {
							if (priority != Priority.NORMAL) {
								throw new WorldConfigurationException("Priority not supported on plugins.");
							}
							dependsOnPlugin(type);
						} else {
							throw new WorldConfigurationException("Unsupported type. Only supports managers and systems.");
						}
				}
			} catch (ReflectionException e) {
				throw new WorldConfigurationException("Unable to instance " + type + " via reflection.", e);
			}
		}
		return this;
	}

	protected void dependsOnManager(int priority, Class<? extends Manager> type) throws ReflectionException {
		if (!containsType(managers, type)) {
			this.managers.add(Registerable.of(ClassReflection.newInstance(type), priority));
		}
	}

	protected void dependsOnSystem(int priority, Class<? extends BaseSystem> type) throws ReflectionException {
		if (!containsType(systems, type)) {
			this.systems.add(Registerable.of(ClassReflection.newInstance(type), priority));
		}
	}

	private void dependsOnPlugin(Class<? extends ArtemisPlugin> type) throws ReflectionException {
		if (!containsType(plugins, type)) {
			this.plugins.add(Registerable.of(ClassReflection.newInstance(type)));
		}
	}

	/**
	 * Register active system(s).
	 * <p/>
	 * Systems track priority separate from manager priority, and are always added after managers.
	 *
	 * Only one instance of each class is allowed.
	 * Use {@see #dependsOn} from within plugins whenever possible.
	 *
	 * @param systems  systems to add, order is preserved.
	 * @param priority priority of added systems, higher priority are added before lower priority.
	 * @return this
	 * @throws WorldConfigurationException if registering the same class twice.
	 */
	public WorldConfigurationBuilder with(int priority, BaseSystem... systems) {
		addSystems(priority, systems, false);
		return this;
	}

	/**
	 * Register active system(s).
	 * <p/>
	 * Systems track priority separate from manager priority, and are always added after managers.
	 *
	 * Only one instance of each class is allowed.
	 * Use {@see #dependsOn} from within plugins whenever possible.
	 *
	 * @param systems systems to add, order is preserved.
	 * @return this
	 * @throws WorldConfigurationException if registering the same class twice.
	 */
	public WorldConfigurationBuilder with(BaseSystem... systems) {
		addSystems(Priority.NORMAL, systems, false);
		return this;
	}


	/**
	 * Add plugins to world.
	 * <p/>
	 * Upon build plugins will be called to register dependencies.
	 *
	 * Only one instance of each class is allowed.
	 * Use {@see #dependsOn} from within plugins whenever possible.
	 *
	 * @param plugins Plugins to add.
	 * @return this
	 * @throws WorldConfigurationException if type is added more than once.
	 */
	public WorldConfigurationBuilder with(ArtemisPlugin... plugins) {
		addPlugins(plugins);
		return this;
	}

	/**
	 * Register passive systems.
	 * <p/>
	 * Systems track priority separate from manager priority, and are always added after managers.
	 *
	 * Only one instance of each class is allowed.
	 * Use {@see #dependsOn} from within plugins.
	 *
	 * @param systems  systems to add, order is preserved.
	 * @param priority priority of added systems, higher priority are added before lower priority.
	 * @return this
	 * @throws WorldConfigurationException if type is added more than once.
	 */
	public WorldConfigurationBuilder withPassive(int priority, BaseSystem... systems) {
		addSystems(priority, systems, true);
		return this;
	}

	/**
	 * Register passive systems with normal priority.
	 * <p/>
	 * Systems track priority separate from manager priority, and are always added after managers.
	 *
	 * Only one instance of each class is allowed.
	 * Use {@see #dependsOn} from within plugins whenever possible.
	 *
	 * @param systems systems to add, order is preserved.
	 * @return this
	 * @throws WorldConfigurationException if type is added more than once.
	 */
	public WorldConfigurationBuilder withPassive(BaseSystem... systems) {
		addSystems(Priority.NORMAL, systems, true);
		return this;
	}

	/**
	 * helper to queue systems for registration.
	 */
	private void addSystems(int priority, BaseSystem[] systems, boolean passive) {
		for (BaseSystem system : systems) {

			if (containsType(this.systems, system.getClass())) {
				throw new WorldConfigurationException("System of type " + system.getClass() + " registered twice. Only once allowed.");
			}

			this.systems.add(new Registerable<>(system, priority, passive));
		}
	}

	/**
	 * Check if bag of registerables contains any of passed type.
	 *
	 * @param items bag of registerables.
	 * @param type  type to check for.
	 * @return {@code true} if found {@code false} if none.
	 */
	@SuppressWarnings("unchecked")
	private boolean containsType(Bag items, Class type) {
		for (Registerable<?> registration : (Bag<Registerable<?>>) items) {
			if (registration.itemType == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add new plugins.
	 */
	private void addPlugins(ArtemisPlugin[] plugins) {
		for (ArtemisPlugin plugin : plugins) {

			if (containsType(this.plugins, plugin.getClass())) {
				throw new WorldConfigurationException("Plugin of type " + plugin.getClass() + " registered twice. Only once allowed.");
			}

			this.plugins.add(Registerable.of(plugin));
		}
	}

	public static abstract class Priority {
		public static final int LOWEST = Integer.MIN_VALUE;
		public static final int LOW = -10000;
		public static final int OPERATIONS = -1000;
		public static final int NORMAL = 0;
		public static final int HIGH = 10000;
		public static final int HIGHEST = Integer.MAX_VALUE;
	}
}
