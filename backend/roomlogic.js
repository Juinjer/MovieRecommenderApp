export class Room {

    constructor(host) {
        this.host = host;
        this.members = [host];
        this.roomId = this.generateId();
    }

    addMember(member) {
        console.log("addmember:" + member)
        this.members.push(member);
    }

    generateId() { // return a 6 digit number
        let min = 100000;
        let max = 999999;
        return Math.floor(Math.random() * (max - min) + min);
    }
}
