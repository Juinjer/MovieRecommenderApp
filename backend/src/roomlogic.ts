export class Room {
    private host: string;
    private members: string[];
    private names: Map<string, string>;
    private roomId: number;
    private nswipes: number;

    private namesOptions: string[] = [
        "Whispering Fox", "Silent Phoenix", "Mystery Hawk", "Shadowed Tiger",
        "Unknown Sparrow", "Veiled Cobra", "Masked Griffin", "Ghostly Pegasus",
        "Cloaked Raven", "Enigma Lynx"
    ];

    constructor(host: string) {
        this.host = host;
        this.members = [host];
        this.names = new Map();
        this.names.set(host, this.getName());
        this.roomId = this.generateId();
        this.nswipes = 5;
    }

    addMember(member: string): void {
        console.log("addmember:" + member);
        this.members.push(member);
        this.names.set(member, this.getName());
    }

    removeMember(member: string): void {
        const memberIndex = this.members.indexOf(member);

        if (memberIndex !== -1) {
            this.members.splice(memberIndex, 1);

            if (this.names.has(member)) {
                this.names.delete(member);
            }
        }
    }

    generateId(): number {
        let min = 100000;
        let max = 999999;
        return Math.floor(Math.random() * (max - min) + min);
    }

    getName(): string {
        const availableNames = this.namesOptions.filter(name => ![...(this.names.values() as Iterable<string>)].includes(name));
        
        if (availableNames.length === 0) {
            return "No available names";
        }

        const randomIndex = Math.floor(Math.random() * availableNames.length);
        return availableNames[randomIndex];
    }

    getHost(): string {
        return this.host;
    }

    getMembers(): string[] {
        return this.members.slice(); // Return a copy to prevent external modification
    }

    getNames(): Map<string, string> {
        return new Map(this.names); // Return a copy to prevent external modification
    }

    getRoomId(): number {
        return this.roomId;
    }

    getNSwipes(): number {
        return this.nswipes;
    }

    setNSwipes(n: number) {
        this.nswipes = n;
    }
}
