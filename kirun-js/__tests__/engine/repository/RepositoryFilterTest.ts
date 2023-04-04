import { KIRunSchemaRepository, KIRunFunctionRepository } from '../../../src';

const funcRepo = new KIRunFunctionRepository();
const schemaRepo = new KIRunSchemaRepository();

test('Repository Filter Test', () => {
    expect(funcRepo.filter('Rep')).toStrictEqual([
        'System.String.Repeat',
        'System.String.Replace',
        'System.String.ReplaceFirst',
        'System.String.PrePad',
        'System.String.ReplaceAtGivenPosition',
    ]);

    expect(funcRepo.filter('root')).toStrictEqual([
        'System.Math.CubeRoot',
        'System.Math.SquareRoot',
    ]);

    expect(schemaRepo.filter('root')).toStrictEqual([]);

    expect(schemaRepo.filter('rin')).toStrictEqual(['System.string']);

    expect(schemaRepo.filter('ny')).toStrictEqual(['System.any']);

    expect(schemaRepo.filter('')).toStrictEqual([
        'System.any',
        'System.boolean',
        'System.double',
        'System.float',
        'System.integer',
        'System.long',
        'System.number',
        'System.string',
        'System.ParameterExpression',
        'System.Null',
        'System.Schema',
    ]);
});
