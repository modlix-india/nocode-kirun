export function generateColor(...str: string[]) {
    return SIDE_COLORS[generateNumber(str.join(''))];
}

export function generateNumber(str: string) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    if (hash < 0) hash = -hash;
    hash += 12;
    return hash % SIDE_COLORS.length;
}

// Modern pastel color palette for node icons
export const SIDE_COLORS = [
    // Blues
    '3B82F6', // Blue 500
    '60A5FA', // Blue 400
    '93C5FD', // Blue 300
    '6366F1', // Indigo 500
    '818CF8', // Indigo 400

    // Greens
    '10B981', // Emerald 500
    '34D399', // Emerald 400
    '6EE7B7', // Emerald 300
    '22C55E', // Green 500
    '4ADE80', // Green 400

    // Purples
    '8B5CF6', // Violet 500
    'A78BFA', // Violet 400
    'C4B5FD', // Violet 300
    'A855F7', // Purple 500
    'C084FC', // Purple 400

    // Pinks/Reds
    'EC4899', // Pink 500
    'F472B6', // Pink 400
    'F9A8D4', // Pink 300
    'F43F5E', // Rose 500
    'FB7185', // Rose 400

    // Oranges/Yellows
    'F97316', // Orange 500
    'FB923C', // Orange 400
    'FDBA74', // Orange 300
    'EAB308', // Yellow 500
    'FACC15', // Yellow 400

    // Teals/Cyans
    '14B8A6', // Teal 500
    '2DD4BF', // Teal 400
    '5EEAD4', // Teal 300
    '06B6D4', // Cyan 500
    '22D3EE', // Cyan 400

    // Grays/Slates
    '64748B', // Slate 500
    '94A3B8', // Slate 400
    '6B7280', // Gray 500
    '9CA3AF', // Gray 400

    // Additional accent colors
    '0EA5E9', // Sky 500
    '38BDF8', // Sky 400
    'D946EF', // Fuchsia 500
    'E879F9', // Fuchsia 400
    '84CC16', // Lime 500
    'A3E635', // Lime 400
];
