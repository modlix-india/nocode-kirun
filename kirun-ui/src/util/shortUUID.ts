const base = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
const baseDivisor = BigInt('' + base.length);
export function shortUUID() {
    let hex = crypto?.randomUUID
        ? crypto.randomUUID().replace(/-/g, '')
        : Math.random().toString(16).substring(2);

    if (BigInt) {
        let num = BigInt('0x' + hex);
        let a = [];

        while (num > 0) {
            a.push(base[Number(num % baseDivisor)]);
            num = num / baseDivisor;
        }

        return a.reverse().join('');
    }

    return hex;
}
