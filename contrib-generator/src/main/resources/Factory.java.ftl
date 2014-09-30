import com.artemis.EntityFactory;
import com.artemis.annotations.CRef;
import com.artemis.annotations.Sticky;
<#list components as c>
import ${c.name};
</#list>

/**
 * Entity factory for ${type}.
 */
@CRef({<#list components as c>${c.simpleName}.class<#if c_has_next>, </#if></#list>})
public interface ${type} extends EntityFactory<${type}> {
    <#list components as c>
    <#list c.methods as m>
    ${type} <#if m.sticky>@Sticky </#if>${m.name}(<#list m.parameters as p>${p.type} ${p.name}<#if p_has_next>, </#if></#list>); <#if m.constructor>// constructor based.</#if>
    </#list>
    </#list>
}