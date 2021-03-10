/**
 * @author jiae
 * 게임 캐릭터의 정보를 가지고 있는 클래스
 */
public class GameCharactor {
    private int level;
    private String name;
    private String weapon;
    private Job job;
    /**
     * 게임 캐릭터를 생성합니다.
     * <p>기본 무기는 목검, 기본 직업은 Beginner입니다.
     * @param name; 캐릭터의 이름; 길이는 3자 이상 10자 이하이어야 합니다.
     * @throws CharactorNameOutboundException 캐릭터의 name 길이가 정해진 범위를 벗어나면, 즉 ({@code name < 3 || name > 10}) 이면 발생합니다.
     */
    public GameCharactor(String name) {
        this.level = 1;
        if (name.length() < 3 || name.length() > 10) throw new CharactorNameOutboundException("캐릭터의 이름은 3자 이상 10자 이하입니다.");
        this.name = name;
        this.weapon = "목검";
        this.job = Job.Beginner;
    }

    /**
     * 캐릭터의 레벨을 반환합니다.
     * @return 캐릭터의 레벨
     */
    public int getLevel(){
        return this.level;
    }

    /**
     * 캐릭터의 직업을 변경합니다.
     * @param job
     * @throws IllegalArgumentException 캐릭터의 레벨이 10이 넘지 않았다면 발생합니다.
     */
    public void setJob(Job job){
        if (this.level < 10) throw new IllegalArgumentException("캐릭터의 레벨이 10을 넘지 않습니다.");
        this.job = job;
    }

    /**
     * 캐릭터의 무기를 변경해주는 메서드입니다.
     * @param weapon 캐릭터가 착용할 무기
     * @throws IllegalArgumentException 캐릭터의 레벨보다 무기의 레벨이 높으면 발생합니다.
     */
    public void setWeapon(String weapon, int weaponLevel){
        if (weaponLevel > this.level) throw new IllegalArgumentException("캐릭터의 레벨보다 무기의 레벨이 높습니다.");
        this.weapon = weapon;
    }

    /**
     * 캐릭터의 레벨을 올려주는 메서드입니다.
     */
    public void levelUp(){
        this.level++;
    }

    /**
     * 캐릭터의 status값을 보여주는 메서드입니다.
     * @return 직업, 레벨, 이름, 무기를 반환합니다.
     */
    public String getCharactorStatus() {
        return "GameCharactor [job=" + job + ", level=" + level + ", name=" + name + ", weapon=" + weapon + "]";
    }
    
    /**
     * 캐릭터의 직업을 나타냅니다.
     */
    public enum Job{
        /** 초보자 */
        Beginner, 
        /** 전사 */
        Warrior, 
        /** 마법사 */
        Wizard, 
        /** 궁수 */
        Archer, 
        /** 도적 */
        Thief
    }
}
