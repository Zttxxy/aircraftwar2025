package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HeroAircraft 单元测试类
 */
class HeroAircraftTest {

    private HeroAircraft heroAircraft;

    @BeforeEach
    void setUp() {
        // 在每个测试前初始化英雄机实例
        heroAircraft = HeroAircraft.getInstance(100, 200, 0, 0, 100);
    }

    @AfterEach
    void tearDown() throws Exception {
        // 在每个测试后重置单例实例，避免测试间相互影响
        Field instanceField = HeroAircraft.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    @DisplayName("测试单例模式 - 多次获取实例应为同一个对象")
    void testGetInstance() {
        // 获取第一个实例
        HeroAircraft instance1 = HeroAircraft.getInstance(150, 250, 0, 0, 100);

        // 获取第二个实例
        HeroAircraft instance2 = HeroAircraft.getInstance();

        // 验证两个实例是同一个对象
        assertSame(instance1, instance2, "单例模式返回的实例应该是同一个对象");

        // 注意：单例模式下，第二次调用getInstance不会更新属性
        // 所以这里我们验证实例的属性是第一次调用时设置的
        assertEquals(100, instance1.getLocationX(), "X坐标应该保持第一次设置的值");
        assertEquals(200, instance1.getLocationY(), "Y坐标应该保持第一次设置的值");
        assertEquals(100, instance1.getHp(), "生命值应该保持第一次设置的值");
    }

    @Test
    @DisplayName("测试单例模式 - 未初始化时调用无参getInstance应抛出异常")
    void testGetInstanceWithoutInitialization() throws Exception {
        // 重置单例实例
        Field instanceField = HeroAircraft.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // 验证未初始化时调用无参getInstance会抛出异常
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            HeroAircraft.getInstance();
        });

        assertEquals("HeroAircraft not initialized. Call getInstance(x,y,speedX,speedY,hp) first.",
                exception.getMessage(), "异常消息应该正确");
    }

    @Test
    @DisplayName("测试射击功能 - 应正确生成子弹")
    void testShoot() {
        // 调用射击方法
        List<BaseBullet> bullets = heroAircraft.shoot();

        // 验证返回的子弹列表不为空
        assertNotNull(bullets, "子弹列表不应该为null");
        assertFalse(bullets.isEmpty(), "子弹列表不应该为空");

        // 验证子弹数量（默认应该是1颗）
        assertEquals(1, bullets.size(), "默认应该发射1颗子弹");

        // 验证子弹属性
        BaseBullet bullet = bullets.get(0);
        assertTrue(bullet instanceof HeroBullet, "子弹应该是HeroBullet类型");
        assertEquals(100, bullet.getLocationX(), "子弹X坐标应该正确");
        assertEquals(198, bullet.getLocationY(), "子弹Y坐标应该正确（向上发射）");
        assertEquals(30, bullet.getPower(), "子弹威力应该正确");
        assertFalse(bullet.notValid(), "新生成的子弹应该是有效的");
    }

    @Test
    @DisplayName("测试减少生命值功能 - 生命值应正确减少")
    void testDecreaseHp() {
        int initialHp = heroAircraft.getHp();

        // 减少生命值
        heroAircraft.decreaseHp(20);

        // 验证生命值正确减少
        assertEquals(initialHp - 20, heroAircraft.getHp(), "生命值应该减少20");

        // 验证飞机没有消失（生命值大于0）
        assertFalse(heroAircraft.notValid(), "生命值大于0时飞机不应该消失");
    }

    @Test
    @DisplayName("测试减少生命值功能 - 生命值为0时应消失")
    void testDecreaseHpToZero() {
        // 减少生命值到0
        heroAircraft.decreaseHp(100);

        // 验证生命值为0
        assertEquals(0, heroAircraft.getHp(), "生命值应该为0");

        // 验证飞机消失
        assertTrue(heroAircraft.notValid(), "生命值为0时飞机应该消失");
    }

    @Test
    @DisplayName("测试减少生命值功能 - 生命值低于0时应消失")
    void testDecreaseHpBelowZero() {
        // 减少超过当前生命值的伤害
        heroAircraft.decreaseHp(150);

        // 验证生命值不会低于0
        assertEquals(0, heroAircraft.getHp(), "生命值不应该低于0");

        // 验证飞机消失
        assertTrue(heroAircraft.notValid(), "生命值低于0时飞机应该消失");
    }

    @Test
    @DisplayName("测试设置生命值功能")
    void testSetHp() {
        // 设置新的生命值
        heroAircraft.setHp(80);

        // 验证生命值被正确设置
        assertEquals(80, heroAircraft.getHp(), "生命值应该被正确设置为80");

        // 设置超过最大生命值
        heroAircraft.setHp(150);

        // 验证生命值可以超过初始最大生命值
        assertEquals(150, heroAircraft.getHp(), "生命值可以超过初始最大生命值");

        // 设置为0
        heroAircraft.setHp(0);
        assertEquals(0, heroAircraft.getHp(), "生命值可以被设置为0");
    }

    @Test
    @DisplayName("测试获取最大生命值")
    void testGetMaxHp() {
        // 验证最大生命值正确
        assertEquals(100, heroAircraft.getMaxHp(), "最大生命值应该正确返回");

        // 修改当前生命值后，最大生命值应该不变
        heroAircraft.setHp(50);
        assertEquals(100, heroAircraft.getMaxHp(), "修改当前生命值后，最大生命值应该不变");
    }

    @Test
    @DisplayName("测试移动功能 - 英雄机不应该自动移动")
    void testForward() {
        int initialX = heroAircraft.getLocationX();
        int initialY = heroAircraft.getLocationY();

        // 调用移动方法
        heroAircraft.forward();

        // 验证位置没有改变（英雄机由鼠标控制，不自动移动）
        assertEquals(initialX, heroAircraft.getLocationX(), "英雄机X坐标不应该改变");
        assertEquals(initialY, heroAircraft.getLocationY(), "英雄机Y坐标不应该改变");
    }

    @Test
    @DisplayName("测试子弹移动功能")
    void testBulletForward() {
        // 获取子弹
        List<BaseBullet> bullets = heroAircraft.shoot();
        BaseBullet bullet = bullets.get(0);

        // 记录初始位置
        int initialX = bullet.getLocationX();
        int initialY = bullet.getLocationY();

        // 移动子弹
        bullet.forward();

        // 验证子弹位置改变（向上移动）
        assertEquals(initialX, bullet.getLocationX(), "子弹X坐标不应该改变");
        assertTrue(bullet.getLocationY() < initialY, "子弹应该向上移动");
    }

    @Test
    @DisplayName("测试单例模式 - 重新初始化应更新属性")
    void testReinitializeSingleton() throws Exception {
        // 重置单例实例
        Field instanceField = HeroAircraft.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // 重新初始化英雄机
        HeroAircraft newInstance = HeroAircraft.getInstance(150, 250, 0, 0, 200);

        // 验证新实例的属性
        assertEquals(150, newInstance.getLocationX(), "重新初始化后X坐标应该更新");
        assertEquals(250, newInstance.getLocationY(), "重新初始化后Y坐标应该更新");
        assertEquals(200, newInstance.getHp(), "重新初始化后生命值应该更新");
    }
}